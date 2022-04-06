package ai.labs.eddi.configs.propertysetter.rest;

import ai.labs.eddi.configs.documentdescriptor.IDocumentDescriptorStore;
import ai.labs.eddi.configs.propertysetter.IPropertySetterStore;
import ai.labs.eddi.configs.propertysetter.IRestPropertySetterStore;
import ai.labs.eddi.configs.propertysetter.model.PropertySetterConfiguration;
import ai.labs.eddi.configs.rest.RestVersionInfo;
import ai.labs.eddi.configs.schema.IJsonSchemaCreator;
import ai.labs.eddi.datastore.IResourceStore;
import ai.labs.eddi.engine.IRestInterfaceFactory;
import ai.labs.eddi.engine.RestInterfaceFactory;
import ai.labs.eddi.models.DocumentDescriptor;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author ginccc
 */

@ApplicationScoped
public class RestPropertySetterStore implements IRestPropertySetterStore {
    private final IPropertySetterStore propertySetterStore;
    private final IJsonSchemaCreator jsonSchemaCreator;
    private final RestVersionInfo<PropertySetterConfiguration> restVersionInfo;
    private IRestPropertySetterStore restPropertySetterStore;

    @Inject
    Logger log;

    @Inject
    public RestPropertySetterStore(IPropertySetterStore propertySetterStore,
                                   IRestInterfaceFactory restInterfaceFactory,
                                   IDocumentDescriptorStore documentDescriptorStore,
                                   IJsonSchemaCreator jsonSchemaCreator) {
        restVersionInfo = new RestVersionInfo<>(resourceURI, propertySetterStore, documentDescriptorStore);
        this.propertySetterStore = propertySetterStore;
        this.jsonSchemaCreator = jsonSchemaCreator;
        initRestClient(restInterfaceFactory);
    }

    private void initRestClient(IRestInterfaceFactory restInterfaceFactory) {
        try {
            restPropertySetterStore = restInterfaceFactory.get(IRestPropertySetterStore.class);
        } catch (RestInterfaceFactory.RestInterfaceFactoryException e) {
            restPropertySetterStore = null;
            log.error(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public Response readJsonSchema() {
        try {
            return Response.ok(jsonSchemaCreator.generateSchema(PropertySetterConfiguration.class)).build();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            throw new InternalServerErrorException();
        }
    }

    @Override
    public List<DocumentDescriptor> readPropertySetterDescriptors(String filter, Integer index, Integer limit) {
        return restVersionInfo.readDescriptors("ai.labs.property", filter, index, limit);
    }

    @Override
    public PropertySetterConfiguration readPropertySetter(String id, Integer version) {
        return restVersionInfo.read(id, version);
    }

    @Override
    public Response updatePropertySetter(String id, Integer version, PropertySetterConfiguration propertySetterConfiguration) {
        return restVersionInfo.update(id, version, propertySetterConfiguration);
    }

    @Override
    public Response createPropertySetter(PropertySetterConfiguration propertySetterConfiguration) {
        return restVersionInfo.create(propertySetterConfiguration);
    }

    @Override
    public Response deletePropertySetter(String id, Integer version) {
        return restVersionInfo.delete(id, version);
    }

    @Override
    public Response duplicatePropertySetter(String id, Integer version) {
        restVersionInfo.validateParameters(id, version);
        PropertySetterConfiguration propertySetterConfiguration = restPropertySetterStore.readPropertySetter(id, version);
        return restPropertySetterStore.createPropertySetter(propertySetterConfiguration);
    }

    @Override
    public String getResourceURI() {
        return restVersionInfo.getResourceURI();
    }

    @Override
    public IResourceStore.IResourceId getCurrentResourceId(String id) throws IResourceStore.ResourceNotFoundException {
        return propertySetterStore.getCurrentResourceId(id);
    }
}
