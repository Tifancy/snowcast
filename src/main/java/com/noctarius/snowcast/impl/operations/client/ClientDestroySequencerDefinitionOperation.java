package com.noctarius.snowcast.impl.operations.client;

import com.hazelcast.client.ClientEndpoint;
import com.hazelcast.instance.MemberImpl;
import com.hazelcast.spi.BackupAwareOperation;
import com.hazelcast.spi.EventRegistration;
import com.hazelcast.spi.EventService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.OperationService;
import com.noctarius.snowcast.impl.NodeSequencerService;
import com.noctarius.snowcast.impl.SequencerDefinition;
import com.noctarius.snowcast.impl.notification.ClientDestroySequencerNotification;
import com.noctarius.snowcast.impl.operations.BackupDestroySequencerDefinitionOperation;
import com.noctarius.snowcast.impl.operations.DestroySequencerOperation;

import java.util.Collection;

import static com.noctarius.snowcast.impl.SnowcastConstants.SERVICE_NAME;

class ClientDestroySequencerDefinitionOperation
        extends AbstractClientRequestOperation
        implements BackupAwareOperation {

    private transient int backupCount;

    ClientDestroySequencerDefinitionOperation(String sequencerName, ClientEndpoint endpoint) {
        super(sequencerName, endpoint);
    }

    @Override
    public void run()
            throws Exception {

        String sequencerName = getSequencerName();

        NodeSequencerService sequencerService = getService();
        SequencerDefinition definition = sequencerService.destroySequencer(sequencerName, true);
        backupCount = definition.getBackupCount();

        NodeEngine nodeEngine = getNodeEngine();

        OperationService operationService = nodeEngine.getOperationService();
        DestroySequencerOperation operation = new DestroySequencerOperation(sequencerName);
        for (MemberImpl member : nodeEngine.getClusterService().getMemberList()) {
            if (!member.localMember()) {
                operationService.invokeOnTarget(SERVICE_NAME, operation, member.getAddress());
            }
        }

        String clientUuid = getEndpoint().getUuid();

        ClientDestroySequencerNotification notification = new ClientDestroySequencerNotification(sequencerName);
        Collection<EventRegistration> registrations = sequencerService.findClientChannelRegistrations(sequencerName, clientUuid);
        EventService eventService = nodeEngine.getEventService();
        eventService.publishEvent(SERVICE_NAME, registrations, notification, 1);
        eventService.deregisterAllListeners(SERVICE_NAME, sequencerName);
    }

    @Override
    public Object getResponse() {
        return Boolean.TRUE;
    }

    @Override
    public boolean shouldBackup() {
        return true;
    }

    @Override
    public int getSyncBackupCount() {
        return backupCount;
    }

    @Override
    public int getAsyncBackupCount() {
        return 0;
    }

    @Override
    public Operation getBackupOperation() {
        return new BackupDestroySequencerDefinitionOperation(getSequencerName());
    }
}
