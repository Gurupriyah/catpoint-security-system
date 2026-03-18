package com.udacity.catpoint.image.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Collectors;

public class AwsImageService implements ImageService {

    private static final Logger log = LoggerFactory.getLogger(AwsImageService.class);

    // AWS recommendation: single client instance
    private static RekognitionClient rekognitionClient;

    public AwsImageService() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {

            if (is == null) {
                log.error("Unable to initialize AWS Rekognition, config.properties not found");
                return;
            }

            props.load(is);

        } catch (IOException ioe) {
            log.error("Unable to initialize AWS Rekognition", ioe);
            return;
        }

        String awsId = props.getProperty("aws.id");
        String awsSecret = props.getProperty("aws.secret");
        String awsRegion = props.getProperty("aws.region");

        AwsCredentials awsCredentials = AwsBasicCredentials.create(awsId, awsSecret);

        rekognitionClient = RekognitionClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(awsRegion))
                .build();
    }

    @Override
    public boolean imageContainsCat(BufferedImage image, float confidenceThreshold) {

        Image awsImage;

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", os);
            awsImage = Image.builder()
                    .bytes(SdkBytes.fromByteArray(os.toByteArray()))
                    .build();
        } catch (IOException ioe) {
            log.error("Error building image byte array", ioe);
            return false;
        }

        DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder()
                .image(awsImage)
                .minConfidence(confidenceThreshold)
                .build();

        DetectLabelsResponse response = rekognitionClient.detectLabels(detectLabelsRequest);
        logLabelsForFun(response);

        return response.labels().stream()
                .anyMatch(label -> label.name().toLowerCase().contains("cat"));
    }

    private void logLabelsForFun(DetectLabelsResponse response) {
        log.info(response.labels().stream()
                .map(label -> String.format("%s(%.1f%%)", label.name(), label.confidence()))
                .collect(Collectors.joining(", ")));
    }
}
