package fr.nefethael.adsbalerter;

import android.location.Location;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CraftHolder {
    private final int BINCRAFT_SIZE = 108;

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    public CraftHolder(byte[] bytes, Location home){
        ByteBuffer wrapped = ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        Log.d("len", String.valueOf(bytes.length));
        int nbCraft = (bytes.length / BINCRAFT_SIZE) - 1;
        for(int i = 0; i < nbCraft; i++){
            int offset = (i * BINCRAFT_SIZE) + BINCRAFT_SIZE;

            int lastIndex = 0;
            wrapped.rewind();
            String hex = String.format("%8X", wrapped.getInt(offset));
            double seen = wrapped.getShort(offset+6) / 10.0;
            double lon = wrapped.getInt(offset+8) /1e6;
            double lat = wrapped.getInt(offset+12) / 1e6;
            int alt = wrapped.getShort(offset+20) * 25;
            String squawk = String.format("%4X", wrapped.getShort(offset+32));
            double gs = wrapped.getShort(offset+34) / 10.0;
            double mach = wrapped.getShort(offset+36) / 1000.0;
            int true_heading = wrapped.getShort(offset+46) / 90;
            short ias = wrapped.getShort(offset+58);
            short dbFlags = wrapped.getShort(offset+86);

            byte[] callsign = Arrays.copyOfRange(bytes, offset+78, offset+78+8);
            byte[] typeCode = Arrays.copyOfRange(bytes, offset+88, offset+88+4);
            byte[] registration = Arrays.copyOfRange(bytes, offset+92, offset+92+12);

            Location pos = new Location("aircraft");
            pos.setAltitude(alt*0.3048);
            pos.setLatitude(lat);
            pos.setLongitude(lon);
            double distance = pos.distanceTo(home);
            double azimuth = pos.bearingTo(home) - true_heading;

            long lastRefresh = System.currentTimeMillis();
            // TODO typeDesc

            StringBuilder sb = new StringBuilder();
            sb.append(hex).append(" ");
            sb.append(seen).append(" ");
            sb.append(lon).append(" ");
            sb.append(lat).append(" ");
            sb.append(alt).append(" ");
            sb.append(squawk).append(" ");
            sb.append(gs).append(" ");
            sb.append(mach).append(" ");
            sb.append(true_heading).append(" ");
            sb.append(ias).append(" ");
            sb.append(dbFlags).append(" ");
            sb.append(distance).append(" ");
            sb.append(azimuth).append(" ");
            sb.append(new String(callsign, StandardCharsets.UTF_8).replace("\u0000", "")).append(" ");
            sb.append(new String(typeCode, StandardCharsets.UTF_8).replace("\u0000", "")).append(" ");
            sb.append(new String(registration, StandardCharsets.UTF_8).replace("\u0000", "")).append(" ");

            Log.d("craft", sb.toString());
        }
    }
}
