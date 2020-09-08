package com.example.ferreteriacontrol;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

public class ProductAdapter extends FirestoreRecyclerAdapter<Product, ProductAdapter.ProductHolder> {
    private boolean includeImage;
    private OnItemClickListener listener;

    public ProductAdapter(@NonNull FirestoreRecyclerOptions<Product> options, boolean b) {
        super(options);
        includeImage = b; //add product image to Recyclerview
    }

    @Override
    protected void onBindViewHolder(@NonNull ProductHolder holder, int position, @NonNull Product model) {
        holder.productName.setText(model.getName());
        holder.productBrand.setText(model.getBrand());
        holder.productPrice.setText(String.valueOf(model.getPrice()) + "$");
        holder.productAmount.setText("Cantidad: " + String.valueOf(model.getAmount()) + " " + model.getUnit());
        holder.productImage.setVisibility(View.INVISIBLE);

        if (includeImage) {
            Picasso.get()
                    .load("https://drive.google.com/uc?id=" + (model.getImage()))//add real url later
                    .resize(200, 200)
                    .centerCrop()
                    .into(holder.productImage);
                    //add default image

            holder.productImage.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item,
                parent, false);
        return new ProductHolder(v);
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    //filterView attempt 1
    public void filterView(Query query){
        FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product.class)
                .build();

       notifyDataSetChanged();//change data again
    }

    class ProductHolder extends RecyclerView.ViewHolder {
        TextView productName, productBrand, productPrice, productAmount, productBsf;
        ImageView productImage;


        public ProductHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productBrand = itemView.findViewById(R.id.productBrand);
            productPrice = itemView.findViewById(R.id.productPrice);
            productAmount = itemView.findViewById(R.id.productAmount);
            productImage = itemView.findViewById(R.id.productImage);
            productBsf = itemView.findViewById(R.id.productBsf);


            //display action on click --> change to title and test it 0)
            productName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position, 1);
                    }
                }
            });


            //display action on click --> change to title and test it 0)
            productBsf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position, 2);
                    }
                }
            });
        }

    }


    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position, int caseView);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
