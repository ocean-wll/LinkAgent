package com.pamirs.attach.plugin.rabbitmq.interceptor;

import com.pamirs.attach.plugin.rabbitmq.RabbitmqConstants;
import com.pamirs.attach.plugin.rabbitmq.destroy.RabbitmqDestroy;
import com.pamirs.pradar.Pradar;
import com.pamirs.pradar.PradarSwitcher;
import com.pamirs.pradar.exception.PressureMeasureError;
import com.pamirs.pradar.interceptor.TraceInterceptorAdaptor;
import com.pamirs.pradar.pressurement.ClusterTestUtils;
import com.shulie.instrument.simulator.api.annotation.Destroyable;
import com.shulie.instrument.simulator.api.listener.ext.Advice;
import org.apache.commons.lang.StringUtils;

/**
 * @author jiangjibo
 * @date 2021/10/14 11:28 上午
 * @description: TODO
 */
@Destroyable(RabbitmqDestroy.class)
public class ChannelNQueueBindInterceptor extends TraceInterceptorAdaptor {

    @Override
    public void beforeFirst(Advice advice) throws Exception {
        ClusterTestUtils.validateClusterTest();
        if (!Pradar.isClusterTest()) {
            return;
        }
        Object[] args = advice.getParameterArray();

        String queue = (String)args[0];
        if (StringUtils.isNotBlank(queue) && !Pradar.isClusterTestPrefix(queue)) {
            args[0] = Pradar.addClusterTestPrefix(queue);
        }

        String exchange = (String)args[1];
        if (StringUtils.isNotBlank(exchange) && !Pradar.isClusterTestPrefix(exchange)) {
            exchange = Pradar.addClusterTestPrefix(exchange);
            args[1] = exchange;
        }

        String routingKey = (String)args[2];
        if (PradarSwitcher.isRabbitmqRoutingkeyEnabled() && StringUtils.isNotBlank(routingKey) && !Pradar.isClusterTestPrefix(routingKey)) {
            routingKey = Pradar.addClusterTestPrefix(routingKey);
            args[2] = routingKey;
        }

        if (StringUtils.isBlank(exchange) && StringUtils.isBlank(routingKey)) {
            throw new PressureMeasureError("RabbitMQ发送压测流量exchange和routingKey值传递同时为空或者空字符串，Pradar拒绝推送");
        }
    }

    @Override
    public String getPluginName() {
        return RabbitmqConstants.PLUGIN_NAME;
    }

    @Override
    public int getPluginType() {
        return RabbitmqConstants.PLUGIN_TYPE;
    }

}
