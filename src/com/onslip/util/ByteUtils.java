
package com.onslip.util;

public abstract class ByteUtils {
    public static byte evenParity(byte b) {
        if ((Integer.bitCount(b & 0x7f) % 2) == 1) {
            b |= 0x80;
        }
        else {
            b &= 0x7f;
        }

        return b;
    }

    public static byte[] evenParity(byte[] bytes) {
        for (int i = 0; i < bytes.length; ++i) {
            bytes[i] = evenParity(bytes[i]);
        }

        return bytes;
    }

    public static boolean checkEvenParity(byte b) {
        return (Integer.bitCount(b & 0xff) % 2) == 1;
    }

    public static boolean checkEvenParity(byte[] bytes) {
        for (int i = 0; i < bytes.length; ++i) {
            if (checkEvenParity(bytes[i])) {
                return false;
            }
        }

        return true;
    }

    public static byte removeParity(byte b) {
        return (byte) (b & 0x7f);
    }

    public static byte[] removeParity(byte[] bytes) {
        for (int i = 0; i < bytes.length; ++i) {
            bytes[i] = removeParity(bytes[i]);
        }

        return bytes;
    }

    private static final char[] hexNibbles = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static char hexNibble(int b) {
        return hexNibbles[b & 15];
    }

    public static String binToHex(byte[] bin) {
        char[] result = new char[bin.length * 2];

        for (int s = 0, d1 = 0, d2 = 1; s < bin.length; ++s, d1 += 2, d2 += 2) {
            result[d1] = hexNibble(bin[s] >> 4);
            result[d2] = hexNibble(bin[s]);
        }

        return new String(result);
    }

    public static byte[] hexToBin(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex digit sequence length must be even");
        }

        byte[] result = new byte[hex.length() / 2];

        for (int s0 = 0, s1 = 1, d = 0; d < result.length; s0 += 2, s1 += 2, ++d) {
            result[d] = (byte) (Character.digit(hex.charAt(s0), 16) * 16 + Character.digit(hex.charAt(s1), 16));
        }

        return result;
    }

    public static String hexDump(byte[] bin) {
        return hexDump(bin, 0, 8, 16);
    }

    public static String hexDump(byte[] bin, int startOffset, int offsetDigits, int bytesPerLine) {
        StringBuilder sb = new StringBuilder();

        String offsetFormat = "%0" + offsetDigits + "x  ";

        for (int i = 0, y = 0; i < bin.length; ++y) {
            sb.append(String.format(offsetFormat, i + startOffset));

            for (int x = 0, j = i; x < bytesPerLine; ++x, ++j) {
                if (j < bin.length) {
                    sb.append(String.format("%02x ", bin[j]));
                }
                else {
                    sb.append("\u25AA\u25AA ");
                }
            }

            sb.append(" ");

            for (int x = 0, j = i; x < bytesPerLine && j < bin.length; ++x, ++j) {
                char c = (char) (bin[j] & 0xff);
                if (Character.isISOControl(c)) {
                    sb.append('\u25AA');
                }
                else {
                    sb.append(c);
                }
            }

            i += bytesPerLine;

            sb.append('\n');
        }

        return sb.toString();
    }
}
