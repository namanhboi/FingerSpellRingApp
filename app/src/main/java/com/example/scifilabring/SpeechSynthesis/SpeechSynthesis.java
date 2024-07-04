package com.example.scifilabring.SpeechSynthesis;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.*;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class SpeechSynthesis {
    // This example requires environment variables named "SPEECH_KEY" and "SPEECH_REGION"
    private static String speechKey = "5c9ea1f866c14609af7f84901c790c65";
    private static String speechRegion = "eastus";

    public static void textToSpeech(String text, Emotion emotion) throws InterruptedException, ExecutionException {
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);

        speechConfig.setSpeechSynthesisVoiceName("en-US-JennyNeural");

        SpeechSynthesizer speechSynthesizer = new SpeechSynthesizer(speechConfig);

        // Get text from the console and synthesize to the default speaker.
//        System.out.println("Enter some text that you want to speak >");
//        String text = new Scanner(System.in).nextLine();
//        if (text.isEmpty())
//        {
//            return;
//        }
        String ssml = generateSSMLString(text, emotion);
        Future speechSynthesisResult = speechSynthesizer.SpeakSsmlAsync(ssml);

//        if (speechSynthesisResult.getReason() == ResultReason.SynthesizingAudioCompleted) {
//            System.out.println("Speech synthesized to speaker for text [" + text + "]");
//        }
//        else if (speechSynthesisResult.getReason() == ResultReason.Canceled) {
//            SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(speechSynthesisResult);
//            System.out.println("CANCELED: Reason=" + cancellation.getReason());
//
//            if (cancellation.getReason() == CancellationReason.Error) {
//                System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
//                System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
//                System.out.println("CANCELED: Did you set the speech resource key and region values?");
//            }
//        }

    }
    public static String generateSSMLString(String text, Emotion emotion) {
        String ssmlTemplate = "<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"https://www.w3.org/2001/mstts\" xml:lang=\"en-US\">\n" +
                "<voice name=\"en-US-JennyNeural\">\n" +
                "        <mstts:express-as style=\"" + emotion + "\">\n" +
                text +
                "        </mstts:express-as>\n" +
                "    </voice>\n" +
                "</speak>";
        return ssmlTemplate;
    }
}
