package hello;

import javax.jms.ConnectionFactory;
import org.apache.qpid.jms.JmsConnectionFactory;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import org.springframework.stereotype.Component;


public class ServiceBusConnectionFactory extends JmsConnectionFactory implements ConnectionFactory {

    public ServiceBusConnectionFactory(final String connectionString, final String clientId) {
        super();
        ConnectionStringBuilder csb = new ConnectionStringBuilder(connectionString);

        String remoteUri = "amqps://"  + csb.getEndpoint().getHost() + "?amqp.idleTimeout=120000&amqp.traceFrames=true";
        this.setRemoteURI(remoteUri);
        this.setClientID(clientId);
        this.setUsername(csb.getSasKeyName());
        this.setPassword(csb.getSasKey());
    }

}
