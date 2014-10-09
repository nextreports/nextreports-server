package ro.nextreports.server.aop;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ConnectionUtil;

@Aspect
public class DataSourceModifiedAdvice {
	
private static final Logger LOG = LoggerFactory.getLogger(DataSourceModifiedAdvice.class);
	
	private StorageService storageService;
	
    @Pointcut("target(ro.nextreports.server.service.StorageService)")
    public void inStorageService() {
    }

    @Pointcut("execution(* modifyEntity(..))")
    public void modifyEntity() {
    }

    @Pointcut("args(source, ..)")
    public void isDataSource(DataSource source) {
    }

    @Pointcut("inStorageService() && modifyEntity() && isDataSource(source)")
    public void dataSourceModified(DataSource source) {
    }
       
    @AfterReturning("dataSourceModified(source)")
    public void afterDataSourceModified(DataSource source) {   
        ConnectionUtil.clearPool(source);
    }
    
    @Required
    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

}
