package fr.nefethael.adsbalerter;

import android.location.Location;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.Map;

public class ADSBRequest extends Request<CraftHolder> {
    private final Map<String, String> headers;
    private final Response.Listener<CraftHolder> listener;
    private final Location home;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param headers Map of request headers
     */
    public ADSBRequest(String url, Map<String, String> headers, Location home,
                       Response.Listener<CraftHolder> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.headers = headers;
        this.listener = listener;
        this.home = home;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected void deliverResponse(CraftHolder response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<CraftHolder> parseNetworkResponse(NetworkResponse response) {
        return Response.success(
                new CraftHolder(response.data, home),
                HttpHeaderParser.parseCacheHeaders(response));

    }
}