package controller;

import model.S3EventParam;
import model.TranscribeParam;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;
import utils.S3EventUtils;
import utils.SecretsManagerUtils;

import java.util.UUID;

public class TranscribeController implements RequestHandler<S3Event, String> {

    @Override
    public String handleRequest(S3Event event, Context context) {

        LambdaLogger logger = context.getLogger();

        //取得觸發S3物件參數
        S3EventUtils s3EventUtils = new S3EventUtils();
        S3EventParam S3EventParam = s3EventUtils.getS3EventParam(event, context);
        String srcBucket = S3EventParam.getSrcBucket();
        String srcKey = S3EventParam.getSrcKey();
        String fileUrl = S3EventParam.getFileUrl();

        String[] srcKeys = srcKey.split("/");
        String path0 = srcKeys[0];
        logger.log("path0 : " + path0);
        String filename = srcKeys[1].split("\\.")[0];
        logger.log("filename : " + filename);

        //取得secretsManager參數
        SecretsManagerUtils secretsManagerUtils = new SecretsManagerUtils();
        TranscribeParam transcribeParam = secretsManagerUtils.getSecret("transcribe/parameter", Region.US_EAST_1, TranscribeParam.class);


        String jobName = UUID.randomUUID().toString();

        StartTranscriptionJobRequest request = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)

                .settings(Settings.builder()
                        .showSpeakerLabels(transcribeParam.isShowSpeakerLabels())
                        .maxSpeakerLabels(transcribeParam.getMaxSpeakerLabels())
                        .channelIdentification(transcribeParam.isChannelIdentification())
                        .showAlternatives(transcribeParam.isShowAlternatives())
                        .build())

                .mediaFormat("mp4")
                .media(Media.builder()
                        .mediaFileUri(fileUrl)
                        .build())

                .subtitles(Subtitles.builder()
                        .formats(SubtitleFormat.SRT, SubtitleFormat.VTT)
                        .build())

                .identifyMultipleLanguages(true)
                .languageOptions(LanguageCode.ZH_TW, LanguageCode.EN_US)

                .outputBucketName(transcribeParam.getSinkBucket())
                .outputKey(path0 + "/" + filename + ".json")
                .build();

        TranscribeClient transcribeClient = TranscribeClient.builder().build();
        transcribeClient.startTranscriptionJob(request);

        return srcBucket + "/" + srcKey;

    }
}