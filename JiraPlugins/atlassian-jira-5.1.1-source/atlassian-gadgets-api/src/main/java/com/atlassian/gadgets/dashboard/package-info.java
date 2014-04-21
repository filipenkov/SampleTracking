/**
 * <p>This package contains the main APIs for interacting with dashboards and gadgets.  Dashboards are represented by
 * immutable state objects, {@link DashboardState}s.  Gadgets are also represented by immutable state objects,
 * {@link GadgetState}s.</p>
 * 
 * <p>A few useful services are defined by this api and are provided by the dashboard-plugin.  They are the
 * {@link DashboardService} and the {@link DashboardStateCache}.</p>
 * 
 * <p>To integrate the dashboards plugin(s) into a host application, an integrator needs to implement the interfaces
 * defined in the {@code com.atlassian.gadgets.spi} package.</p>
 * 
 * <p>A note about serialization: {@link GadgetId}, {@link DashboardId}, {@link GadgetState} and {@link DashboardState}
 * all implement the Serializable marker interface.  However, Java serialization is not meant to be used as way of
 * doing long term persistence of these objects.  They implement the {@link Serializable} interface purely for
 * distribution among remote systems that might be sharing a cache or need to transfer these objects for other reasons.
 * Again, it is not meant to be used as a means of persisting these objects between JVM restarts.</p>
 */

@ParametersAreNonnullByDefault
/**
 * Parameters are non-null by default, as declared by the annotation above.  Return values should also be considered
 * to be non-null unless they are annotated with {@link Nullable}.  Unfortunately, there is no way to declare this with
 * annotations at present.
 */
package com.atlassian.gadgets.dashboard;

import javax.annotation.ParametersAreNonnullByDefault;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetState;

