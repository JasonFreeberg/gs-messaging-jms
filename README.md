# Spring, JMS, ServiceBus Developer Scenario

> This example project is from the official [Spring JMS starter project](https://spring.io/guides/gs/messaging-jms/
).

## Current Experience:
This written from the perspective of a novice developer taking the Spring Messaging example project and integrating it with ServiceBus. Before starting the process, the project works using an embedded broker to send the message. Now we will try to extend the project to use AMQP and ServiceBus. The `complete/` directory was the starting point, and `complete2/` was after doing these steps.

1. The Spring Messaging documentation says the module [supports AMQP via the RabbitMQ client](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-messaging.html#boot-features-amqp).
1. Then [this Microsoft documentation](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-jms-api-amqp) tells me that ServiceBus supports AMQP 1.0 for JMS. So on the surface it looks like this should work.
1. Per the Microsoft docs, I add the Apache Qpid dependency to my project. (I'm already asking why I can't just use ActiveMQ, the client provided by the Spring module.)
    1. **Minor Blocker**: It tells me to install four JAR files but only lists two...
    1. The docs show me a download location for the JARs, but I would rather use Maven to control my dependencies, luckily I find [this doc](https://qpid.apache.org/maven.html) via Google that gives me the Maven coordinates.

        ```xml
        <dependency>
          <groupId>org.apache.qpid</groupId>
          <artifactId>qpid-client</artifactId>
          <version>6.3.3</version>
        </dependency>
        <dependency>
          <groupId>org.apache.geronimo.specs</groupId>
          <artifactId>geronimo-jms_1.1_spec</artifactId>
          <version>1.0</version>
        </dependency>
        ```

1. **Blocker**: The ServiceBus docs do not have *anything* for Spring, so I am on my own at this point.
1. After reading [other Spring docs](https://docs.spring.io/spring/docs/5.1.5.RELEASE/spring-framework-reference/integration.html#jms-jmstemplate) and asking [smarter people](https://www.yevster.com/) for help, I figure out that I need to provide my own `ConnectionFactory`:

    ```java
    import javax.jms.ConnectionFactory;
    import org.apache.qpid.jms.JmsConnectionFactory
    import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;

    public class ServiceBusConnectionFactory extends JmsConnectionFactory implements ConnectionFactory{
        public ServiceBusConnectionFactory(final String connectionString, final String clientId) {
            super();
            ConnectionStringBuilder csb = new ConnectionStringBuilder(connectionString);
            String remoteUri = "amqps://" + csb.getEndpoint().getHost() + "?amqp.idleTimeout=120000&amqp.traceFrames=true";
            this.setRemoteURI(remoteUri);
            this.setClientID(clientId);
            this.setUsername(csb.getSasKeyName());
            this.setPassword(csb.getSasKey());
        }
    }
    ```

1. Configure the JmsTemplate to use the custom ConnectionFactory. This object is eventually used to send and receive the messages.

    ```java
    @Bean
    public JmsTemplate jmsTemplate() throws NamingException {
        return new JmsTemplate(new CachingConnectionFactory(new ServiceBusConnectionFactory(connectionString, clientId)));
    }
    ```

1. The ServiceBus namespace must..
    - allow access from all networks.
    - be Premium or higher (lower tiers have some limitations)
    - have a queue named "mailbox" to match the starter code
    - have an access policy with read/write access for the "mailbox" queue

    Then paste the connection string in the constructor of the connection factory.

1. For this code example, the `Email` class must also extend `Serializable`.

## Gaps to address:

- Documentation:
    - Link to the Maven coordinates for Qpid dependencies from Service Bus documentation
    - Until we have a better solution, we should document the process outlined in this README
- Tools:
    - We should distribute the `ServiceBusConnectionFactory` in Maven (and provide documentation on how to use it)

https://github.com/tabish121/qpid-jms-spring-boot