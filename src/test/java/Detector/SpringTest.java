package Detector;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.univjfc.si.monitoring.opennms.detector.OncRpc.OncRpcDetector;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;


// Test if spring is able to load the detector class
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/detectors.xml",
		//"classpath:/default-foreign-source.xml"
})
public class SpringTest implements ApplicationContextAware {
	//JMK : to do use simple server
	
	private  OncRpcDetector m_detector;
	private ApplicationContext m_applicationContext;
	private Object m_server;

	
	@Before
    public void prepare() throws Exception{
        MockLogAppender.setupLogging();
        m_detector = getDetector(OncRpcDetector.class);
        m_detector.setTimeout(500);
    }
	@Test
	public void test() {
	    
		ForeignSource m_foreignSource = new ForeignSource();
		PluginConfig config = new PluginConfig();
		config.setPluginClass("fr.univjfc.si.monitoring.opennms.detector.OncRpc.OncRpcDetector");
		Set<String> keys = config.getAvailableParameterKeys();
		assertEquals(11,keys.size());
		System.out.println(keys);
        m_foreignSource.addDetector(config);
		
	}

	
	private OncRpcDetector getDetector(Class<? extends ServiceDetector> detectorClass) {
		Object bean = m_applicationContext.getBean(detectorClass.getName());
		
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (OncRpcDetector)bean;
	}




	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	
		m_applicationContext = applicationContext;
	
	}

}
