package twin.developers.projectmqtt;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Inicio extends AppCompatActivity implements SensorEventListener {

    private DatabaseReference databaseReference;
    private Button enviarButton;
    private Button btnLimpiar;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView variationTextView, stateTextView, resultTextView;
    private Button startButton;
    private float lastX, lastY, lastZ;
    private boolean isMeasuring = false;
    private static final float THRESHOLD_NORMAL = 0.0f;
    private static final float THRESHOLD_LEVE = 3.0f;
    private static final float THRESHOLD_CRITICO = 5.0f;
    private static final long MEASUREMENT_DURATION = 5000;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        btnLimpiar = findViewById(R.id.btnlimpiar);
        variationTextView = findViewById(R.id.variationTextView);
        stateTextView = findViewById(R.id.stateTextView);
        resultTextView = findViewById(R.id.txtresultado);
        startButton = findViewById(R.id.startButton);
        enviarButton = findViewById(R.id.btnsend);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMeasurement();
            }
        });
        Button btnir = findViewById(R.id.btnsoporte);
        btnir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Inicio.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btnLimpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                variationTextView.setText("");
                stateTextView.setText("");
                resultTextView.setText("");
                stopMeasurement();
            }
        });
        enviarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Mensaje = resultTextView.getText().toString().trim();
                Datos datos = new Datos(Mensaje);
                databaseReference.push().setValue(datos);
            }
        });
    }

    private void startMeasurement() {
        if (!isMeasuring) {
            isMeasuring = true;
            startButton.setText("Stop");

            resetValues();
            registerSensor();

            countDownTimer = new CountDownTimer(MEASUREMENT_DURATION, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    stopMeasurement();
                    displayResult();
                }
            }.start();
        } else {
            stopMeasurement();
        }
    }

    private void stopMeasurement() {
        if (isMeasuring) {
            isMeasuring = false;
            startButton.setText("Start");
            unregisterSensor();
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
        }
    }

    private void displayResult() {
        float variation = obtenerVariacionActual();
        final String result;
        if (variation == THRESHOLD_NORMAL) {
            result = "Resultado: Normal";
        } else if (variation <= THRESHOLD_LEVE) {
            result = "Resultado: Leve";
        } else if (variation <= THRESHOLD_CRITICO) {
            result = "Resultado: Crítico";
        } else {
            result = "Resultado: Normal";
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultTextView.setText(result);
            }
        });
    }

    private float obtenerVariacionActual() {
        return Math.abs(lastX - lastY - lastZ);
    }

    private void resetValues() {
        lastX = 0;
        lastY = 0;
        lastZ = 0;
    }

    private void registerSensor() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void unregisterSensor() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float variation = obtenerVariacionActual();
        variationTextView.setText("Variation: " + variation);

        if (variation == THRESHOLD_NORMAL) {
            stateTextView.setText("State: Normal");
        } else if (variation <= THRESHOLD_LEVE) {
            stateTextView.setText("State: Leve");
        } else if (variation <= THRESHOLD_CRITICO) {
            stateTextView.setText("State: Crítico");
        }

        lastX = x;
        lastY = y;
        lastZ = z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
