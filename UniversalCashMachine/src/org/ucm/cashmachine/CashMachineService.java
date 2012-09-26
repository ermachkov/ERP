/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ubo.utils.SystemXML;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class CashMachineService implements Service {

    private static CashMachineService self = null;
    private SystemXML sXML;
    private Path pathToSystemXML;

    private CashMachineService(Path pathToSystemXML) {
        this.pathToSystemXML = pathToSystemXML;
        sXML = SystemXML.newSystemXML(pathToSystemXML);
    }

    public synchronized static CashMachineService getInstance(Path pathToSystemXML) {
        if (self == null) {
            self = new CashMachineService(pathToSystemXML);
        }

        return self;
    }

    @Override
    public String[] getAvialableDrivers() {
        return new String[]{"atol", "shtrih"};
    }

    @Override
    public boolean setDriver(String cashMachineName, String driverName) {
        Map<String, String> map = new HashMap<>();
        map.put("driver", driverName);
        return sXML.setValue("/root/cashmachine[@name='" + cashMachineName + "']", map);
    }

    @Override
    public String getDriver(String cashMachineName) {
        return sXML.getValue("/root/cashmachine[@name='" + cashMachineName + "']/@driver", false);
    }

    public boolean isPresent(String xPathExpression) {
        return sXML.isPresent(xPathExpression);
    }

    @Override
    public boolean addCashMachine(String cashMachineName, String driverName) {
        if (isPresent("//root/cashmachine[@name='" + cashMachineName + "']")) {
            return true;
        }

        Map<String, String> map = new HashMap<>();
        map.put("name", cashMachineName);
        map.put("driver", driverName);
        return sXML.addNode("/root", "cashmachine", map);
    }

    @Override
    public boolean setDefaultCashMachine(String cashMachineName) {
        if (isPresent("//root/default_cashmachine")) {
            return true;
        }

        Map<String, String> map = new HashMap<>();
        map.put("name", cashMachineName);
        return sXML.addNode("/root", "default_cashmachine", map);
    }

    @Override
    public String getDefaultCashMachine() {
        return sXML.getValue("/root/default_cashmachine/@name", false);
    }

    @Override
    public List<String> getCashMachines() {
        return sXML.getValues("//root/cashmachine/@name", false);
    }

    @Override
    public boolean setSpeed(String cashMachineName, int speed) {
        Map<String, String> attr = new HashMap<>();
        attr.put("speed", "" + speed);
        return sXML.setValue("/root/cashmachine[@name='" + cashMachineName + "']", attr);
    }

    @Override
    public int getSpeed(String cashMachineName) {
        int speed = -1;
        String s = sXML.getValue("/root/cashmachine[@name='" + cashMachineName + "']/@speed", false);
        try {
            if (s != null) {
                speed = Integer.parseInt(s);
            }

        } finally {
            return speed;
        }
    }

    @Override
    public boolean setPort(String cashMachineName, String port) {
        Map<String, String> attr = new HashMap<>();
        attr.put("port", port);
        return sXML.setValue("/root/cashmachine[@name='" + cashMachineName + "']", attr);
    }

    @Override
    public String getPort(String cashMachineName) {
        return sXML.getValue("/root/cashmachine[@name='" + cashMachineName + "']/@port", false);
    }

    @Override
    public boolean setPassword(String cashMachineName, int password) {
        Map<String, String> attr = new HashMap<>();
        attr.put("password", "" + password);
        return sXML.setValue("/root/cashmachine[@name='" + cashMachineName + "']", attr);
    }

    @Override
    public int getPassword(String cashMachineName) {
        int password = -1;
        try {
            String s = sXML.getValue("/root/cashmachine[@name='" + cashMachineName + "']/@password", false);
            password = Integer.parseInt(s);

        } finally {
            return password;
        }
    }

    @Override
    public int getMaxTextFieldLenght(String cashMachineName) {
        int len = 20;

        try {
            String s = sXML.getValue("/root/cashmachine[@name='" + cashMachineName + "']/@text_field_lenght", false);
            len = Integer.parseInt(s);

        } finally {
            return len;
        }

    }

    @Override
    public String getConfigDir(String cashMachineName) {
        String s = sXML.getValue("/root/cashmachine[@name='" + cashMachineName + "']"
                + "/@config_dir", false);
        Path p = pathToSystemXML.subpath(0, pathToSystemXML.getNameCount() - 1);
        String configDir = Paths.get(pathToSystemXML.getRoot().toString(), 
                p.toString(), "cashmachine", s).toString();
        return configDir;
    }

    @Override
    public boolean setConfigDir(String cashMachineName, String folderName) {
        Map<String, String> attr = new HashMap<>();
        attr.put("config_dir", folderName);
        return sXML.setValue("/root/cashmachine/[@name='" + cashMachineName + "'", attr);
    }
}
