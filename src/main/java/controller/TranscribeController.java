package controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.LanguageCode;
import software.amazon.awssdk.services.transcribe.model.Media;
import software.amazon.awssdk.services.transcribe.model.StartTranscriptionJobRequest;
import java.util.UUID;

public class TranscribeController implements RequestHandler<S3Event, String> {

    @Override
    public String handleRequest(S3Event event, Context context) {

        S3EventNotificationRecord record = event.getRecords().get(0);
        String srcBucket = record.getS3().getBucket().getName();
        String srcKey = record.getS3().getObject().getUrlDecodedKey();
        String region = record.getAwsRegion();
        String fileUrl = "s3://" + srcBucket + "/" + srcKey;

        LambdaLogger logger = context.getLogger();
        logger.log("RECORD: " + record);
        logger.log("SOURCE BUCKET: " + srcBucket);
        logger.log("SOURCE KEY: " + srcKey);
        logger.log("S3 Region: " + region);
        logger.log("FILE URL: " + fileUrl);

        String jobName = UUID.randomUUID().toString();

        StartTranscriptionJobRequest request = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .mediaFormat("mp3")
                .languageCode(LanguageCode.ZH_TW)
                .outputBucketName(srcBucket)
                .outputKey(srcKey + ".json")
                .media(Media.builder()
                        .mediaFileUri(fileUrl)
                        .build())
                .build();

        TranscribeClient transcribeClient = TranscribeClient.builder().build();
        transcribeClient.startTranscriptionJob(request);

        return srcBucket + "/" + srcKey;

    }
}