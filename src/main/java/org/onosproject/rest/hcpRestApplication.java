package org.onosproject.rest;

import org.onlab.rest.AbstractWebApplication;

import java.util.Set;

/**
 * @Author ldy
 * @Date: 2021/3/3 下午3:00
 * @Version 1.0
 */
public class hcpRestApplication extends AbstractWebApplication {

    @Override
    public Set<Class<?>> getClasses() {
        return getClasses(hcpRestResource.class);
    }
}
