/*
 * Copyright (c) 2014, Christoph Engelbert (aka noctarius) and
 * contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.noctarius.snowcast.impl.operations;

import com.hazelcast.instance.MemberImpl;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.OperationService;
import com.noctarius.snowcast.impl.NodeSequencerService;
import com.noctarius.snowcast.impl.SequencerDataSerializerHook;

public class DestroySequencerDefinitionOperation
        extends AbstractSequencerOperation {

    public DestroySequencerDefinitionOperation() {
    }

    public DestroySequencerDefinitionOperation(String sequencerName) {
        super(sequencerName);
    }

    @Override
    public int getId() {
        return SequencerDataSerializerHook.TYPE_DESTROY_SEQUENCER_DEFINITION;
    }

    @Override
    public void run()
            throws Exception {

        NodeSequencerService sequencerService = getService();
        sequencerService.destroySequencer(getSequencerName(), true);

        NodeEngine nodeEngine = getNodeEngine();
        OperationService operationService = nodeEngine.getOperationService();

        DestroySequencerOperation operation = new DestroySequencerOperation(getSequencerName());
        for (MemberImpl member : nodeEngine.getClusterService().getMemberList()) {
            if (!member.localMember() && !member.getAddress().equals(getCallerAddress())) {
                operationService.invokeOnTarget(getServiceName(), operation, member.getAddress());
            }
        }
    }

    @Override
    public boolean returnsResponse() {
        return true;
    }

    @Override
    public Object getResponse() {
        return Boolean.TRUE;
    }
}
