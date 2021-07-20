package com.example.marvinclient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.util.TimeUnit;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CommandBuilderDialogFragment extends DialogFragment {

    private ProgressBar commandProgressProgressBar;
    private ProgressBar commandSendProgressBar;
    private EditText commandEditText;

    private Future<SpeechRecognitionResult> task;
    private boolean isListening = false;
    private MutableLiveData<SpeechRecognitionResult> speechResult;
    private boolean stopTimer = false;

    public interface CommandBuilderDialogListener {
        void onDialogPositiveClick(String buildCommand);
    }

    CommandBuilderDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (CommandBuilderDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Classes using this must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View commandBuilderView = inflater.inflate(R.layout.dialog_commander, null);
        builder.setView(commandBuilderView);
        builder.setPositiveButton("Send Command", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDialogPositiveClick(commandEditText.getText().toString());
            }
        });

        commandProgressProgressBar = commandBuilderView.findViewById(R.id.commandProgressProgressBar);
        commandProgressProgressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isListening) {
                    stopListening();
                }
            }
        });

        commandSendProgressBar = commandBuilderView.findViewById(R.id.commandSendProgressBar);
        commandSendProgressBar.setVisibility(View.INVISIBLE);

        commandEditText = commandBuilderView.findViewById(R.id.commandEditText);
        commandEditText.setEnabled(false);
        commandEditText.setVisibility(View.INVISIBLE);
        commandEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer = true;
            }
        });

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        startListening();
    }

    private void startListening() {
        isListening = true;

        try {
            SpeechConfig config = SpeechConfig.fromSubscription(getString(R.string.azure_speech_to_text), "uksouth");
            assert(config != null);

            SpeechRecognizer reco = new SpeechRecognizer(config);
            assert(reco != null);

            task = reco.recognizeOnceAsync();
            assert(task != null);

            MutableLiveData<SpeechRecognitionResult> speechResult = getSpeechResult();
            speechResult.observe(this, result -> {
                if (result.getReason() == ResultReason.RecognizedSpeech) {
                    commandEditText.setText(result.getText());
                    startSendingCommand();
                }
                else {
                    Toast.makeText(getContext(), "Error recognizing. Did you update the subscription info?" + System.lineSeparator() + result.toString(), Toast.LENGTH_SHORT).show();
                }

                reco.close();
            });

        } catch (Exception ex) {
            Toast.makeText(getContext(), "unexpected " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            assert(false);
        }
    }

    private MutableLiveData<SpeechRecognitionResult> getSpeechResult() {
        if (speechResult == null) {
            speechResult = new MutableLiveData<SpeechRecognitionResult>();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SpeechRecognitionResult result = task.get();
                    speechResult.postValue(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return speechResult;
    }

    private void stopListening() {
        isListening = false;
        task.cancel(true);
        commandProgressProgressBar.setVisibility(View.INVISIBLE);
        commandEditText.setEnabled(true);
        commandEditText.setVisibility(View.VISIBLE);
    }

    private void startSendingCommand() {
        isListening = false;
        commandProgressProgressBar.setVisibility(View.INVISIBLE);
        commandEditText.setEnabled(true); // TODO: Can i make isListening watchable and change the visibility of views depending on that?
        commandEditText.setVisibility(View.VISIBLE);
        commandSendProgressBar.setVisibility(View.VISIBLE);

        startSendCountdown();
    }

    private void startSendCountdown() {
        CommandBuilderDialogFragment me = this;

        CountDownTimer sendTimer = new CountDownTimer(2000, 20) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (stopTimer) {
                    commandSendProgressBar.setProgress(0);
                    commandSendProgressBar.setVisibility(View.INVISIBLE);
                }
                else {
                    commandSendProgressBar.incrementProgressBy(1);
                }
            }

            @Override
            public void onFinish() {
                if (stopTimer) {
                    commandSendProgressBar.setProgress(0);
                    commandSendProgressBar.setVisibility(View.INVISIBLE);
                }
                else {
                    commandSendProgressBar.setProgress(100);
                    listener.onDialogPositiveClick(commandEditText.getText().toString());
                    me.dismiss();
                }
            }
        };
        sendTimer.start();
    }

}
