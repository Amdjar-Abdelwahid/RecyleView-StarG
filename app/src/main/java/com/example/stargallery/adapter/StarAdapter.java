// 5. adapter/StarAdapter.java
package com.example.stargallery.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.stargallery.R;
import com.example.stargallery.beans.Star;
import com.example.stargallery.service.StarService;

import java.util.ArrayList;
import java.util.List;

public class StarAdapter extends RecyclerView.Adapter<StarAdapter.StarViewHolder> implements Filterable {
    private static final String TAG = "StarAdapter";
    private List<Star> stars;
    private List<Star> starsFilter;
    private Context context;
    private NewFilter mfilter;

    public StarAdapter(Context context, List<Star> stars) {
        this.stars = stars;
        this.context = context;
        starsFilter = new ArrayList<>();
        starsFilter.addAll(stars);
        mfilter = new NewFilter(this);
    }

    @NonNull
    @Override
    public StarViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(this.context).inflate(R.layout.star_item, viewGroup, false);

        final StarViewHolder holder = new StarViewHolder(v);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    View popup = LayoutInflater.from(context).inflate(R.layout.star_edit_item, null, false);
                    final ImageView img = popup.findViewById(R.id.img);
                    final RatingBar bar = popup.findViewById(R.id.ratingBar);
                    final TextView idss = popup.findViewById(R.id.idss);

                    // Récupération sécurisée de l'image
                    ImageView originalImage = v.findViewById(R.id.img);
                    if (originalImage != null) {
                        Drawable drawable = originalImage.getDrawable();
                        if (drawable instanceof BitmapDrawable) {
                            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                            img.setImageBitmap(bitmap);
                        } else {
                            // Fallback si ce n'est pas un BitmapDrawable
                            img.setImageResource(R.mipmap.star);
                        }
                    }

                    // Récupération sécurisée du rating
                    RatingBar originalRating = v.findViewById(R.id.stars);
                    if (originalRating != null) {
                        bar.setRating(originalRating.getRating());
                    }

                    // Récupération sécurisée de l'ID
                    TextView idView = v.findViewById(R.id.ids);
                    String idText = "0";
                    if (idView != null && idView.getText() != null) {
                        idText = idView.getText().toString();
                    }
                    idss.setText(idText);

                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Notez : ")
                            .setMessage("Donner une note entre 1 et 5 :")
                            .setView(popup)
                            .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        float s = bar.getRating();
                                        int ids = Integer.parseInt(idss.getText().toString());
                                        Star star = StarService.getInstance().findById(ids);
                                        if (star != null) {
                                            star.setStar(s);
                                            StarService.getInstance().update(star);
                                            notifyItemChanged(holder.getAdapterPosition());
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Erreur lors de la mise à jour de la note: " + e.getMessage());
                                    }
                                }
                            })
                            .setNegativeButton("Annuler", null)
                            .create();
                    dialog.show();
                } catch (Exception e) {
                    Log.e(TAG, "Erreur lors de l'affichage du dialogue: " + e.getMessage());
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull StarViewHolder starViewHolder, int i) {
        Log.d(TAG, "onBindView call ! "+ i);
        try {
            Glide.with(context)
                    .asBitmap()
                    .load(starsFilter.get(i).getImg())
                    .apply(new RequestOptions().override(100, 100))
                    .into(starViewHolder.img);

            starViewHolder.name.setText(starsFilter.get(i).getName().toUpperCase());
            starViewHolder.stars.setRating(starsFilter.get(i).getStar());
            starViewHolder.idss.setText(starsFilter.get(i).getId()+"");
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du binding des données: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return starsFilter.size();
    }

    @Override
    public Filter getFilter() {
        return mfilter;
    }

    public class StarViewHolder extends RecyclerView.ViewHolder {
        TextView idss;
        ImageView img;
        TextView name;
        RatingBar stars;
        RelativeLayout parent;

        public StarViewHolder(@NonNull View itemView) {
            super(itemView);
            idss = itemView.findViewById(R.id.ids);
            img = itemView.findViewById(R.id.img);
            name = itemView.findViewById(R.id.name);
            stars = itemView.findViewById(R.id.stars);
            parent = itemView.findViewById(R.id.parent);
        }
    }

    public class NewFilter extends Filter {
        public RecyclerView.Adapter mAdapter;

        public NewFilter(RecyclerView.Adapter mAdapter) {
            super();
            this.mAdapter = mAdapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            starsFilter.clear();
            final FilterResults results = new FilterResults();
            if (charSequence.length() == 0) {
                starsFilter.addAll(stars);
            } else {
                final String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Star p : stars) {
                    if (p.getName().toLowerCase().startsWith(filterPattern)) {
                        starsFilter.add(p);
                    }
                }
            }
            results.values = starsFilter;
            results.count = starsFilter.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            starsFilter = (List<Star>) filterResults.values;
            this.mAdapter.notifyDataSetChanged();
        }
    }
}