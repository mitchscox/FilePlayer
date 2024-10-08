package org.example;

import org.apache.logging.log4j.*;
import javax.jms.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;


public class JsonFileProcessor {

    private final ConnectionFactory connectionFactory;
    private final String inputDir;
    private final String outputDir;
    private static final String INPUT_DIR = "/home/bugeye2/IdeaProjects/FilePlayer/input";
    private static final String OUTPUT_DIR = "/home/bugeye2/IdeaProjects/FilePlayer/output";
    Logger logger = LogManager.getLogger();
    /* The following will be needed if we want to process the data here
     Such use cases would be header incrementing or file incrementing */

    // private final ObjectMapper objectMapper;

    public JsonFileProcessor(ConnectionFactory connectionFactory, String inputDir, String outputDir) {
        this.connectionFactory = connectionFactory;
        this.inputDir = inputDir;
        this.outputDir = outputDir;

        // see declarations comment
        //this.objectMapper = new ObjectMapper();
    }

    public void processFiles() throws JMSException, IOException {
        Path inputDir = Paths.get(INPUT_DIR);
        Path outputDir = Paths.get(OUTPUT_DIR);

       // TODO fix directory existence check
       /* if (!inputDir.exists() || !inputDir.isDirectory()) {
            throw new IllegalArgumentException("Input directory does not exist or is not a directory.");
        }*/

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            // todo check if files already exist
            inputDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            logger.info("Created the watch service for : " +inputDir);
            while (true) {
                WatchKey key = watchService.poll(10, TimeUnit.SECONDS);
                logger.info("Polling event ... ");

                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        logger.info("Detected Event :" +event.kind());
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fileName = ev.context();
                        Path filePath = inputDir.resolve(fileName);

                        if (Files.isRegularFile(filePath) && fileName.toString().endsWith(".json")) {
                            sendFileToQueue(filePath);
                            processAndMoveFile(filePath, outputDir);
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
           logger.error("Exception: " +e);
           // e.printStackTrace();
        }
    }

    private void sendFileToQueue(Path file) throws JMSException, IOException {
            Connection connection = connectionFactory.createConnection();
            connection.start();
            logger.info("Connection started: " +connection);
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("FilePlayer");
            MessageProducer producer = session.createProducer(queue);

            String jsonContent = new String(Files.readAllBytes(file));
            TextMessage message = session.createTextMessage(jsonContent);
            producer.send(message);
          //  System.out.println("Sent file to queue: " + file);
            logger.info("Sent file: "+file+"to queue:" +queue);
            connection.close();
            logger.info("Connection closed: "+ connection);

    }

    private void processAndMoveFile(Path filePath, Path outputDir) {
        try {
            Path targetPath = outputDir.resolve(filePath.getFileName());
            Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Moved file: "+filePath+" to:" +targetPath);
        } catch (IOException e) {
            logger.error("Error processing file: " + filePath);
            logger.error("Exception: " +e);
        }

    }
    public static void main(String[] args) {
        try {
            ConnectionFactory connectionFactory = JMSConfig.connectionFactory();
            String inputDir = "/home/bugeye2/IdeaProjects/FilePlayer/input";
            String outputDir = "/home/bugeye2/IdeaProjects/FilePlayer/output";
            JsonFileProcessor processor = new JsonFileProcessor(connectionFactory, inputDir, outputDir);
            processor.processFiles();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}