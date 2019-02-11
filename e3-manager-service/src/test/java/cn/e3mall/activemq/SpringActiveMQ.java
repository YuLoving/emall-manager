package cn.e3mall.activemq;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**  

* <p>Title: SpringActiveMQ</p>  

* <p>Description: </p>  

* @author 赵天宇

* @date 2019年1月10日  

*/
public class SpringActiveMQ {
	
	@Test
	public void sendMession() throws Exception{
	//初始化spring容器
	ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-activemq.xml");
	//从容器中获取jmsTemplate对象
	JmsTemplate jmsTemplate = ac.getBean(JmsTemplate.class);
	//从容器中获取Destination对象
	Destination destination = (Destination) ac.getBean("queueDestination");
	//发送消息
	jmsTemplate.send(destination, new MessageCreator() {
		
		@Override
		public Message createMessage(Session session) throws JMSException {
			//创建一个消息对象并返回
			return session.createTextMessage("send activemq queue message");
		}
	});
	}
}
