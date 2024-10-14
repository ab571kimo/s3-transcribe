package controller;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.AmazonTranscribeClientBuilder;
import com.amazonaws.services.transcribe.model.LanguageCode;
import com.amazonaws.services.transcribe.model.Media;
import com.amazonaws.services.transcribe.model.Settings;
import com.amazonaws.services.transcribe.model.StartTranscriptionJobRequest;

import java.io.InputStream;
import java.util.UUID;

public class S3_Transcribe implements RequestHandler<S3Event, String>{
	
	@Override
	public String handleRequest(S3Event event, Context context) {
	      LambdaLogger logger = context.getLogger();
	      S3EventNotificationRecord record = (S3EventNotificationRecord)event.getRecords().get(0);
	      String srcBucket = record.getS3().getBucket().getName();
	      String srcKey = record.getS3().getObject().getUrlDecodedKey();
	      logger.log("RECORD: " + record);
	      logger.log("SOURCE BUCKET: " + srcBucket);
	      logger.log("SOURCE KEY: " + srcKey);
	
	      AmazonTranscribe amazonTranscribe = AmazonTranscribeClientBuilder.standard().withRegion(Regions.US_WEST_2).build();
	      
	      String jobName = UUID.randomUUID().toString();
	      StartTranscriptionJobRequest request = new StartTranscriptionJobRequest();
	      
	      request.setTranscriptionJobName(jobName);
	      request.setMediaFormat("mp3");
	      request.withLanguageCode(LanguageCode.ZhTW);

	      Settings channel_settings = new Settings();
	      channel_settings.setChannelIdentification(true);
	      channel_settings.withChannelIdentification(true);

	      Media media = new Media();
	      media.setMediaFileUri("s3://"+srcBucket+"/"+srcKey);
	      request.setMedia(media);
	      request.setOutputBucketName(srcBucket);
	      
	      amazonTranscribe.startTranscriptionJob(request);
	      
	      //request.withMedia(media);
	      //request.setTranscriptionJobName(jobName);
	      //request.withMediaFormat(getFileFormat(guid));
	      //request.withSettings(channel_settings);
	      //asyncClient.startTranscriptionJobAsync(request, new AsyncTranscriptionJobHandler());
	      
	      
	      
	      return srcBucket + "/" + srcKey;
	   }
}
