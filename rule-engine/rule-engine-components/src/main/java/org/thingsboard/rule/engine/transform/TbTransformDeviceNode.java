/**
 * Copyright © 2016-2020 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.rule.engine.transform;

import static org.thingsboard.rule.engine.api.TbRelationTypes.SUCCESS;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import org.thingsboard.rule.engine.api.RuleNode;
import org.thingsboard.rule.engine.api.TbContext;
import org.thingsboard.rule.engine.api.TbNode;
import org.thingsboard.rule.engine.api.TbNodeConfiguration;
import org.thingsboard.rule.engine.api.TbNodeException;
import org.thingsboard.rule.engine.api.util.TbNodeUtils;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.plugin.ComponentType;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.dao.device.DeviceService;

@RuleNode(
        type = ComponentType.TRANSFORMATION,
        name = "change device by name",
        configClazz = TbTransformMsgNodeConfiguration.class,
        nodeDescription = "Change Message device by Metadata's deviceName",
        nodeDetails = "",
        uiResources = {"static/rulenode/rulenode-core-config.js", "static/rulenode/rulenode-core-config.css"},
        configDirective = "tbTransformationNodeScriptConfig")
public class TbTransformDeviceNode implements TbNode {

	private static final String DEVICE_NAME = "deviceName";
	private TbTransformMsgNodeConfiguration config;
	
	@Override
	public void init(TbContext ctx, TbNodeConfiguration configuration) throws TbNodeException {
		this.config = TbNodeUtils.convert(configuration, TbTransformMsgNodeConfiguration.class);
	}

	@Override
	public void onMsg(TbContext ctx, TbMsg msg) throws ExecutionException, InterruptedException, TbNodeException {
		DeviceService deviceService = ctx.getDeviceService();
		Device device = deviceService.findDeviceByTenantIdAndName(ctx.getTenantId(), msg.getMetaData().getValue(DEVICE_NAME));
		
		if(device != null) {
			TbMsg newMsg = ctx.transformMsg(msg, msg.getType(), device.getId(), msg.getMetaData(), msg.getData());
			ctx.tellNext(newMsg, SUCCESS);
		} else {
			ctx.tellFailure(msg, new NoSuchElementException("Can't find device: " + msg.getMetaData().getValue(DEVICE_NAME)));
		}
	}

	@Override
	public void destroy() {

	}

}
