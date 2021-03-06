/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.netconf.notifications.impl.osgi;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;
import org.opendaylight.controller.netconf.api.Capability;
import org.opendaylight.controller.netconf.api.monitoring.CapabilityListener;
import org.opendaylight.controller.netconf.api.util.NetconfConstants;
import org.opendaylight.controller.netconf.mapping.api.NetconfOperation;
import org.opendaylight.controller.netconf.mapping.api.NetconfOperationService;
import org.opendaylight.controller.netconf.mapping.api.NetconfOperationServiceFactory;
import org.opendaylight.controller.netconf.notifications.NetconfNotification;
import org.opendaylight.controller.netconf.notifications.NetconfNotificationCollector;
import org.opendaylight.controller.netconf.notifications.impl.NetconfNotificationManager;
import org.opendaylight.controller.netconf.notifications.impl.ops.CreateSubscription;
import org.opendaylight.controller.netconf.notifications.impl.ops.Get;
import org.opendaylight.controller.netconf.util.capability.BasicCapability;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    private ServiceRegistration<NetconfNotificationCollector> netconfNotificationCollectorServiceRegistration;
    private ServiceRegistration<NetconfOperationServiceFactory> operationaServiceRegistration;
    private NetconfNotificationManager netconfNotificationManager;

    @Override
    public void start(final BundleContext context) throws Exception {
        netconfNotificationManager = new NetconfNotificationManager();
        netconfNotificationCollectorServiceRegistration = context.registerService(NetconfNotificationCollector.class, netconfNotificationManager, new Hashtable<String, Object>());

        final NetconfOperationServiceFactory netconfOperationServiceFactory = new NetconfOperationServiceFactory() {

            private final Set<Capability> capabilities = Collections.<Capability>singleton(new BasicCapability(NetconfNotification.NOTIFICATION_NAMESPACE));

            @Override
            public Set<Capability> getCapabilities() {
                return capabilities;
            }

            @Override
            public AutoCloseable registerCapabilityListener(final CapabilityListener listener) {
                listener.onCapabilitiesAdded(capabilities);
                return new AutoCloseable() {
                    @Override
                    public void close() {
                        listener.onCapabilitiesRemoved(capabilities);
                    }
                };
            }

            @Override
            public NetconfOperationService createService(final String netconfSessionIdForReporting) {
                return new NetconfOperationService() {

                    private final CreateSubscription createSubscription = new CreateSubscription(netconfSessionIdForReporting, netconfNotificationManager);


                    @Override
                    public Set<NetconfOperation> getNetconfOperations() {
                        return Sets.<NetconfOperation>newHashSet(
                                new Get(netconfSessionIdForReporting, netconfNotificationManager),
                                createSubscription);
                    }

                    @Override
                    public void close() {
                        createSubscription.close();
                    }
                };
            }
        };

        final Dictionary<String, String> properties = new Hashtable<>();
        properties.put(NetconfConstants.SERVICE_NAME, NetconfConstants.NETCONF_MONITORING);
        operationaServiceRegistration = context.registerService(NetconfOperationServiceFactory.class, netconfOperationServiceFactory, properties);

    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        if(netconfNotificationCollectorServiceRegistration != null) {
            netconfNotificationCollectorServiceRegistration.unregister();
            netconfNotificationCollectorServiceRegistration = null;
        }
        if (netconfNotificationManager != null) {
            netconfNotificationManager.close();
        }
        if (operationaServiceRegistration != null) {
            operationaServiceRegistration.unregister();
            operationaServiceRegistration = null;
        }
    }
}
