package com.example.scolaris_auth.service;

import com.example.scolaris_auth.dto.request.RegisterRequest;
import com.example.scolaris_auth.exception.AppException;
import com.example.scolaris_auth.model.enums.Role;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    @Value("${keycloak.server-url}")   private String serverUrl;
    @Value("${keycloak.realm}")        private String realm;
    @Value("${keycloak.client-id}")    private String clientId;
    @Value("${keycloak.client-secret}") private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    private Keycloak getAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    public String createKeycloakUser(String username, String password, String firstName, String lastName, Role role, String groupName) {
        UsersResource users = getAdminClient().realm(realm).users();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(password);
        cred.setTemporary(false);
        user.setCredentials(List.of(cred));

        Response response = users.create(user);
        if (response.getStatus() != 201) {
            throw new AppException("Keycloak error: " + response.getStatusInfo());
        }

        String userId = response.getLocation().getPath().replaceAll(".*/", "");

        RoleRepresentation roleRep = getAdminClient().realm(realm).roles()
                .get(role.name()).toRepresentation();
        users.get(userId).roles().realmLevel().add(List.of(roleRep));

        if (groupName != null && !groupName.isEmpty()) {
            List<GroupRepresentation> groups = getAdminClient().realm(realm).groups().groups(groupName, 0, 1);
            if (!groups.isEmpty()) {
                users.get(userId).joinGroup(groups.get(0).getId());
            }
        }

        return userId;
    }

    public AccessTokenResponse getToken(String username, String password) {
        try (Keycloak kc = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .grantType(OAuth2Constants.PASSWORD)
                .build()) {
            return kc.tokenManager().getAccessToken();
        }
    }

    public AccessTokenResponse refreshToken(String refreshToken) {
        String url = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("refresh_token", refreshToken);
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);

        return restTemplate.postForObject(url, map, AccessTokenResponse.class);
    }

    public void updateKeycloakUser(String keycloakId, String firstName, String lastName, String username, String groupName) {
        UserResource userResource = getAdminClient().realm(realm).users().get(keycloakId);
        UserRepresentation user = userResource.toRepresentation();

        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (username != null) {
            user.setUsername(username);
            user.setEmail(username);
        }
        userResource.update(user);

        if (groupName != null) {
            List<GroupRepresentation> currentGroups = userResource.groups();
            for (GroupRepresentation g : currentGroups) {
                userResource.leaveGroup(g.getId());
            }
            List<GroupRepresentation> groups = getAdminClient().realm(realm).groups().groups(groupName, 0, 1);
            if (!groups.isEmpty()) {
                userResource.joinGroup(groups.get(0).getId());
            }
        }
    }

    public void changeUserRole(String keycloakId, Role newRole) {
        UsersResource users = getAdminClient().realm(realm).users();
        List<RoleRepresentation> existing = users.get(keycloakId).roles().realmLevel().listAll();
        existing.removeIf(r -> r.getName().equals("default-roles-" + realm) || r.getName().equals("offline_access") || r.getName().equals("uma_authorization"));
        users.get(keycloakId).roles().realmLevel().remove(existing);

        RoleRepresentation role = getAdminClient().realm(realm).roles()
                .get(newRole.name()).toRepresentation();
        users.get(keycloakId).roles().realmLevel().add(List.of(role));
    }

    public void resetKeycloakPassword(String keycloakId, String newPassword) {
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(newPassword);
        cred.setTemporary(false);
        getAdminClient().realm(realm).users().get(keycloakId).resetPassword(cred);
    }

    public void disableUser(String keycloakId) {
        UserResource userResource = getAdminClient().realm(realm).users().get(keycloakId);
        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(false);
        userResource.update(user);
    }

    public String createKeycloakGroup(String groupName) {
        GroupRepresentation group = new GroupRepresentation();
        group.setName(groupName);
        Response response = getAdminClient().realm(realm).groups().add(group);
        if (response.getStatus() != 201) {
            throw new AppException("Keycloak group creation error: " + response.getStatusInfo());
        }
        return response.getLocation().getPath().replaceAll(".*/", "");
    }

    public void updateKeycloakGroup(String keycloakGroupId, String newName) {
        GroupRepresentation group = getAdminClient().realm(realm).groups().group(keycloakGroupId).toRepresentation();
        group.setName(newName);
        getAdminClient().realm(realm).groups().group(keycloakGroupId).update(group);
    }

    public void deleteKeycloakGroup(String keycloakGroupId) {
        getAdminClient().realm(realm).groups().group(keycloakGroupId).remove();
    }
}