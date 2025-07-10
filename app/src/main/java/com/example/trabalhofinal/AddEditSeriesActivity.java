package com.example.trabalhofinal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEditSeriesActivity extends AppCompatActivity {

    private TextInputEditText editTextTitle;
    private TextInputEditText editTextGenre;
    private TextInputEditText editTextSeasons;
    private Button buttonCancel;
    private Button buttonOk;
    private ImageView imagePicker; // Referência ao ImageView do ícone da câmera

    private SeriesDbHelper dbHelper;
    private long seriesId = -1; // -1 indicates adding a new series, otherwise editing existing
    private String currentImagePath = null; // Para armazenar o caminho da imagem

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int REQUEST_CODE_TAKE_PHOTO = 101;
    private static final int REQUEST_CODE_PERMISSIONS = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_series);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
        }

        editTextTitle = findViewById(R.id.edit_text_series_title);
        editTextGenre = findViewById(R.id.edit_text_genre);
        editTextSeasons = findViewById(R.id.edit_text_seasons);
        buttonCancel = findViewById(R.id.button_cancel);
        buttonOk = findViewById(R.id.button_ok);
        imagePicker = findViewById(R.id.image_picker); // Inicializa o ImageView

        dbHelper = new SeriesDbHelper(this);

        // Check if we are editing an existing series
        if (getIntent().hasExtra("SERIES_ID")) {
            seriesId = getIntent().getLongExtra("SERIES_ID", -1);
            String title = getIntent().getStringExtra("SERIES_TITLE");
            String genre = getIntent().getStringExtra("SERIES_GENRE");
            int seasons = getIntent().getIntExtra("SERIES_SEASONS", 0);
            String imagePath = getIntent().getStringExtra("SERIES_IMAGE_PATH"); // Obtém o caminho da imagem

            editTextTitle.setText(title);
            editTextGenre.setText(genre);
            editTextSeasons.setText(String.valueOf(seasons));
            currentImagePath = imagePath; // Define o caminho da imagem atual

            // Exibir a imagem se houver uma
            if (currentImagePath != null && !currentImagePath.isEmpty()) {
                loadImageFromPath(currentImagePath);
            }
        }

        buttonCancel.setOnClickListener(v -> finish());
        buttonOk.setOnClickListener(v -> saveSeries());

        // Lógica para clicar no ícone da câmera
        imagePicker.setOnClickListener(v -> {
            checkPermissionsAndPickImage();
        });
    }

    private void checkPermissionsAndPickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    REQUEST_CODE_PERMISSIONS);
        } else {
            showImagePickerDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerDialog();
            } else {
                Toast.makeText(this, "Permissões de armazenamento e câmera são necessárias para selecionar uma imagem.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolher Imagem")
                .setItems(new CharSequence[]{"Tirar Foto", "Escolher da Galeria"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Tirar Foto
                            dispatchTakePictureIntent();
                            break;
                        case 1: // Escolher da Galeria
                            dispatchPickImageIntent();
                            break;
                    }
                })
                .show();
    }

    private void dispatchPickImageIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_CODE_PICK_IMAGE);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Garante que haja um aplicativo de câmera para lidar com a intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Erro ao criar o arquivo
                Toast.makeText(this, "Erro ao criar arquivo de imagem.", Toast.LENGTH_SHORT).show();
            }
            // Continue apenas se o arquivo foi criado com sucesso
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Cria um nome de arquivo de imagem
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefixo */
                ".jpg",         /* sufixo */
                storageDir      /* diretório */
        );

        // Salva o caminho do arquivo:
        currentImagePath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_IMAGE && data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    currentImagePath = selectedImageUri.toString(); // Salva a URI da imagem da galeria
                    loadImageFromUri(selectedImageUri);
                }
            } else if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
                // A imagem já foi salva no currentImagePath pelo createImageFile()
                loadImageFromPath(currentImagePath);
            }
        } else if (resultCode == RESULT_CANCELED) {
            // Se o usuário cancelou a captura/seleção, e se a imagem era de uma foto recém-tirada,
            // pode ser necessário limpar o currentImagePath se a foto não foi usada.
            // Para simplificar, vamos manter o currentImagePath se já houver um,
            // ou definir para null se foi uma nova tentativa de foto cancelada.
            if (requestCode == REQUEST_CODE_TAKE_PHOTO && currentImagePath != null && new File(currentImagePath).exists()) {
                // Se a foto foi tirada mas a operação cancelada, e o arquivo existe, podemos mantê-lo
                // ou até apagá-lo se quisermos ser mais rigorosos.
                // Por agora, vamos apenas deixá-lo, o usuário pode tentar novamente.
            } else {
                // Se o usuário cancelou a seleção da galeria ou uma nova foto
                // sem uma imagem anterior, podemos limpar o ImageView
                // imagePicker.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera));
                // currentImagePath = null; // Cuidado ao limpar, pode apagar imagem existente se for edição.
            }
        }
    }

    private void loadImageFromUri(Uri uri) {
        try {
            // Este método é mais genérico, pode lidar com Uri da galeria
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            imagePicker.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar imagem.", Toast.LENGTH_SHORT).show();
            imagePicker.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera)); // Volta ao padrão
            currentImagePath = null;
        }
    }

    private void loadImageFromPath(String path) {
        if (path == null || path.isEmpty()) {
            imagePicker.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera));
            return;
        }

        // Tenta carregar como um URI primeiro (para imagens da galeria)
        try {
            Uri uri = Uri.parse(path);
            if (uri != null && uri.getScheme() != null) { // Verifica se é um URI válido com esquema
                loadImageFromUri(uri);
                return;
            }
        } catch (Exception e) {
            // Ignora, não é um URI, tenta carregar como caminho de arquivo
        }

        // Se não for um URI, tenta carregar como caminho de arquivo absoluto
        File imgFile = new File(path);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imagePicker.setImageBitmap(myBitmap);
        } else {
            Toast.makeText(this, "Imagem não encontrada no caminho: " + path, Toast.LENGTH_SHORT).show();
            imagePicker.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera)); // Volta ao padrão
            currentImagePath = null; // Limpa o caminho inválido
        }
    }


    private void saveSeries() {
        String title = editTextTitle.getText().toString().trim();
        String genre = editTextGenre.getText().toString().trim();
        String seasonsStr = editTextSeasons.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("O título é obrigatório");
            return;
        }

        int seasons = 0;
        if (!TextUtils.isEmpty(seasonsStr)) {
            try {
                seasons = Integer.parseInt(seasonsStr);
            } catch (NumberFormatException e) {
                editTextSeasons.setError("Número de temporadas inválido");
                return;
            }
        }

        Series series = new Series(title, genre, seasons, currentImagePath); // Passa o caminho da imagem

        if (seriesId == -1) {
            // Add new series
            long newId = dbHelper.addSeries(series);
            if (newId != -1) {
                Toast.makeText(this, "Série adicionada com sucesso!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Erro ao adicionar série.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Update existing series
            series.setId(seriesId);
            int rowsAffected = dbHelper.updateSeries(series);
            if (rowsAffected > 0) {
                Toast.makeText(this, "Série atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Erro ao atualizar série.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Handle back button in toolbar
        return true;
    }
}