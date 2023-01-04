package com.bedrockcloud.bedrockcloud.server.proxy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ProxyServerProvider {
    public Map<String, ProxyServer> proxyServerMap;
    
    public ProxyServerProvider() {
        this.proxyServerMap = new HashMap<String, ProxyServer>();
    }
    
    public Map<String, ProxyServer> getProxyServerMap() {
        return this.proxyServerMap;
    }
    
    public void addProxyServer(final ProxyServer proxyServer) {
        this.proxyServerMap.put(proxyServer.getServerName(), proxyServer);
    }
    
    public void removeServer(final ProxyServer proxyServer) {
        this.proxyServerMap.remove(proxyServer.getServerName());
    }
    
    public void removeServer(final String name) {
        this.proxyServerMap.remove(name);
    }
    
    public ProxyServer getProxyServer(final String name) {
        return this.proxyServerMap.get(name);
    }
    
    public boolean existServer(final String name) {
        return this.proxyServerMap.get(name) != null;
    }
    
    public void deleteServer(final File file, final String serverName) {
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (file.isDirectory()) {
            final String[] fileList = file.list();
            if (fileList.length == 0) {
                file.delete();
            }
            else {
                for (final String fileName : fileList) {
                    final String fullPath = file.getPath() + "/" + fileName;
                    final File fileOrFolder = new File(fullPath);
                    this.delete(fileOrFolder);
                }
                this.delete(file);
            }
        } else {
            file.delete();
        }
    }
    
    public void delete(final File file) {
        if (file.isDirectory()) {
            final String[] fileList = file.list();
            if (fileList.length == 0) {
                file.delete();
            }
            else {
                for (final String fileName : fileList) {
                    final String fullPath = file.getPath() + "/" + fileName;
                    final File fileOrFolder = new File(fullPath);
                    this.delete(fileOrFolder);
                }
                this.delete(file);
            }
        } else {
            file.delete();
        }
    }
}
