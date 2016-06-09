package org.deeplearning4j;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.model.RouteDefinition;
import org.apache.hadoop.yarn.webapp.hamlet.HamletSpec;
import org.canova.api.writable.Writable;

import java.util.Collection;
import java.util.UUID;

/**
 * A Camel Java DSL Router
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CamelKafkaRouteBuilder extends RouteBuilder {
    private String topicName;
    private String kafkaBrokerList;
    private static RecordSerializer serializer = new RecordSerializer();
    private String writableConverter = "org.canova.api.io.converters.SelfWritableConverter";
    private String canovaMarshaller = "org.canova.camel.component.csv.marshaller.ListStringInputMarshaller";
    private String inputUri;
    private String inputFormat;
    private Processor processor;
    private String dataTypeUnMarshal;


    /**
     * Let's configure the Camel routing rules using Java code...
     */
    @Override
    public void configure() {
        RouteDefinition def = from(inputUri);
        if(dataTypeUnMarshal != null)
            def = def.unmarshal(dataTypeUnMarshal);


        def = def.to(String.format("canova://%s?inputMarshaller=%s&writableConverter=%s",inputFormat,canovaMarshaller,writableConverter));
        if(processor != null)
            def = def.process(processor);

        def = def.to(String.format("kafka:%s?topic=%s",
                        kafkaBrokerList,
                        topicName))
                .errorHandler(loggingErrorHandler("org.deeplearning4j").level(LoggingLevel.INFO));


    }



    public void setContext(CamelContext camelContext) {
        super.setContext(camelContext);
    }



    public static class Builder {
        private String writableConverter = "org.canova.api.io.converters.SelfWritableConverter";
        private String canovaMarshaller = "org.canova.camel.component.csv.marshaller.ListStringInputMarshaller";
        private String inputUri;
        private String topicName;
        private String kafkaBrokerList = "localhost:9092";
        private CamelContext camelContext;
        private String inputFormat;
        private Processor processor;
        private String dataTypeUnMarshal;


        public Builder processor(Processor processor) {
            this.processor = processor;
            return this;
        }

        public Builder kafkaBrokerList(String kafkaBrokerList) {
            this.kafkaBrokerList = kafkaBrokerList;
            return this;
        }

        public Builder inputFormat(String inputFormat) {
            this.inputFormat = inputFormat;
            return this;
        }

        public Builder camelContext(CamelContext camelContext) {
            this.camelContext = camelContext;
            return this;
        }

        public Builder inputUri(String inputUri) {
            this.inputUri = inputUri;
            return this;
        }

        public Builder writableConverter(String writableConverter) {
            this.writableConverter = writableConverter;
            return this;
        }


        public Builder canovaMarshaller(String canovaMarshaller) {
            this.canovaMarshaller = canovaMarshaller;
            return this;
        }

        public Builder dataTypeUnMarshal(String dataTypeUnMarshal) {
            this.dataTypeUnMarshal = dataTypeUnMarshal;
            return this;
        }


        public Builder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        private void assertStringNotNUllOrEmpty(String value,String name)  {
            if(value == null || value.isEmpty())
                throw new IllegalStateException(String.format("Please define a %s",name));

        }

        public CamelKafkaRouteBuilder build() {
            CamelKafkaRouteBuilder routeBuilder;
            assertStringNotNUllOrEmpty(inputUri,"input uri");
            assertStringNotNUllOrEmpty(topicName,"topic name");
            assertStringNotNUllOrEmpty(kafkaBrokerList,"kafka broker");
            assertStringNotNUllOrEmpty(inputFormat,"input format");
            routeBuilder = new CamelKafkaRouteBuilder(
                    topicName,
                    kafkaBrokerList,
                    writableConverter,
                    canovaMarshaller,
                    inputUri,
                    inputFormat
                    ,processor,
                    dataTypeUnMarshal);
            if(camelContext != null)
                routeBuilder.setContext(camelContext);
            return routeBuilder;
        }

    }




}