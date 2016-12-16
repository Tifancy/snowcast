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
package com.noctarius.snowcast.impl.operations.clientcodec;

import com.noctarius.snowcast.impl.NodeSequencerService;

import javax.annotation.Nonnull;

public class ClientRemoveChannelOperation
        extends AbstractClientRequestOperation {

    protected String sequencerName;
    protected String registrationId;

    public ClientRemoveChannelOperation(@Nonnull String sequencerName, @Nonnull MessageChannel messageChannel,
                                        @Nonnull String registrationId) {

        super(sequencerName, messageChannel);
        this.sequencerName = sequencerName;
        this.registrationId = registrationId;
    }

    @Override
    public void run()
            throws Exception {

        NodeSequencerService sequencerService = getService();
        sequencerService.unregisterClientChannel(sequencerName, registrationId);
    }

    @Override
    public Object getResponse() {
        return Boolean.TRUE;
    }
}