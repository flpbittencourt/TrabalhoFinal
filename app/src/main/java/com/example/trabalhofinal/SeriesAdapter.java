package com.example.trabalhofinal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class SeriesAdapter extends RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder> {

    private List<Series> seriesList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Series series);
        void onItemLongClick(Series series);
    }

    public SeriesAdapter(List<Series> seriesList, OnItemClickListener listener) {
        this.seriesList = seriesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_series, parent, false);
        return new SeriesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeriesViewHolder holder, int position) {
        Series series = seriesList.get(position);
        holder.titleTextView.setText(series.getTitle());
        holder.genreTextView.setText(series.getGenre());
        holder.seasonsTextView.setText(String.format(holder.itemView.getContext().getString(R.string.seasons_format), series.getSeasons()));
        // Atualiza a exibição da imagem
        loadImageToImageView(holder.seriesImageThumbnail, series.getImagePath());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(series));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(series);
            return true;
        });
    }

    private void loadImageToImageView(ImageView imageView, String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            imageView.setImageDrawable(ContextCompat.getDrawable(imageView.getContext(), R.drawable.ic_camera));
            return;
        }

        // Tenta carregar como um URI primeiro (para imagens da galeria)
        try {
            Uri uri = Uri.parse(imagePath);
            if (uri != null && uri.getScheme() != null) { // Verifica se é um URI válido com esquema
                imageView.setImageURI(uri);
                return;
            }
        } catch (Exception e) {
            // Ignora, não é um URI, tenta carregar como caminho de arquivo
        }

        // Se não for um URI, tenta carregar como caminho de arquivo absoluto
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(imageView.getContext(), R.drawable.ic_camera));
        }
    }


    @Override
    public int getItemCount() {
        return seriesList.size();
    }

    static class SeriesViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView genreTextView;
        TextView seasonsTextView;
        ImageView seriesImageThumbnail; // Adicionado

        public SeriesViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.series_title);
            genreTextView = itemView.findViewById(R.id.series_genre);
            seasonsTextView = itemView.findViewById(R.id.series_seasons);
            seriesImageThumbnail = itemView.findViewById(R.id.series_image_thumbnail); // Inicializado
        }
    }
}