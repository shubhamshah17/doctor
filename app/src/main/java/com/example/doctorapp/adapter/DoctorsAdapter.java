
package com.example.doctorapp.adapter;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorapp.BookingSlots;
import com.example.doctorapp.R;
import com.example.doctorapp.models.Doctor;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class DoctorsAdapter extends RecyclerView.Adapter<DoctorsAdapter.ProductViewHolder> {

    private Context mCtx;
    private List<Doctor> productList;


    public DoctorsAdapter(Context mCtx, List<Doctor> productList) {
        this.mCtx = mCtx;
        this.productList = productList;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.list_layout, null);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Doctor product = productList.get(position);

        holder.doctorname.setText(product.getDoctorname());
        holder.doctorcategory.setText(product.getDoctorcategory());
        holder.doctordegree.setText(String.valueOf(product.getDoctordegree()));
        holder.docotrExp.setText(String.valueOf(product.getDocotrExp()));
        holder.ratingbar.setRating(Float.parseFloat(String.valueOf(product.getRating())));

        holder.imageView.setImageDrawable(mCtx.getResources().getDrawable(product.getImage()));

    }


    @Override
    public int getItemCount() {
        return productList.size();
    }


    class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView doctorname, doctorcategory, doctordegree, docotrExp;
        RatingBar ratingbar;
        CircleImageView imageView;
        Button callnow, bookappoint;

        public ProductViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            doctorname = itemView.findViewById(R.id.doctorname);
            doctorcategory = itemView.findViewById(R.id.doctorcategory);
            doctordegree = itemView.findViewById(R.id.doctordegree);
            docotrExp = itemView.findViewById(R.id.docotrExp);
            ratingbar = itemView.findViewById(R.id.ratingbar);
            callnow = itemView.findViewById(R.id.callnow);
            bookappoint = itemView.findViewById(R.id.bookappoint);

            callnow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + 9987661990L));
                    mCtx.startActivity(callIntent);
                }
            });
            bookappoint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mCtx, BookingSlots.class);
                    mCtx.startActivity(intent);

                }
            });
        }

        @Override
        public void onClick(View v) {

        }
    }
}