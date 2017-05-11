package net.anotheria.moskito.extension.nginx.config;

import com.google.gson.annotations.SerializedName;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.anotheria.moskito.extension.nginx.NginxMonitor;
import org.configureme.annotations.AfterReConfiguration;
import org.configureme.annotations.Configure;
import org.configureme.annotations.ConfigureMe;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Configuration bean for NginxMonitorPlugin.
 *
 * @author dzhmud
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
@SuppressWarnings(value = "unused")
@ConfigureMe(name = "nginx-monitor")
public class NginxMonitorConfig implements Serializable {

    /**
     * Default update period in seconds.
     */
    private static final long DEFAULT_UPDATE_PERIOD = 60;

    /**
     * Update period in seconds.
     */
    @Configure
    private long updatePeriod = -1;

    /**
     * Array of NginxMonitoredInstance configurations.
     */
    @Configure
    @SerializedName("@monitoredInstances")
    private NginxMonitoredInstance[] monitoredInstances;

    public long getUpdatePeriod() {
        return updatePeriod > 0 ? updatePeriod : DEFAULT_UPDATE_PERIOD;
    }

    public void setUpdatePeriod(long updatePeriod) {
        this.updatePeriod = updatePeriod;
    }

    public NginxMonitoredInstance[] getMonitoredInstances() {
        return monitoredInstances;
    }

    public void setMonitoredInstances(NginxMonitoredInstance[] instances) {
        this.monitoredInstances = instances;
    }

    /**
     * On runtime reconfiguration, trigger reinitializing of producers/timers/etc.
     */
    @AfterReConfiguration
    public void afterReConfiguration() {
        NginxMonitor.reconfigureNginxMonitor(this);
    }

    @Override
    public String toString() {
        return "NginxMonitorConfig{" +
                "updatePeriod=" + updatePeriod + ", " +
                "instances=" + Arrays.toString(monitoredInstances) +
                '}';
    }
}
