package io.sls.permission;

import io.sls.persistence.IResourceStore;

import java.net.URI;

/**
 * @author ginccc
 */
public interface IAuthorizationManager {
    boolean isUserAuthorized(String resourceId, Integer version, URI user, IAuthorization.Type authorizationType) throws IResourceStore.ResourceStoreException, IResourceStore.ResourceNotFoundException;
}
