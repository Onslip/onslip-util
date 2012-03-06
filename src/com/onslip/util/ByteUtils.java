
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

    public static String binToHex(byte[] bin, char separator) {
        String result = binToHex(bin);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < result.length(); ++i) {
            if (sb.length() != 0 && (i % 2) == 0) {
                sb.append(separator);
            }

            sb.append(result.charAt(i));
        }

        return sb.toString();
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

    public static int binToInt(byte[] bin, boolean bigEndian) {
        if (bin.length > 4) {
            throw new IllegalArgumentException("Size of bin array larger than 4");
        }

        return (int)  binToLong(bin, bigEndian);
    }

    public static long binToLong(byte[] bin, boolean bigEndian) {
        if (bin.length > 8) {
            throw new IllegalArgumentException("Size of bin array larger than 8");
        }

        long out = 0;

        for (int i = 0; i < bin.length; ++i) {
            int offset = bigEndian ? i : bin.length - 1 - i;

            out = (out << 8) | (bin[offset] & 0xff);
        }

        return out;
    }

    public static byte[] intToBin(int dec, boolean bigEndian) {
        return toBin(dec, bigEndian, new byte[4]);
    }

    public static byte[] longToBin(long dec, boolean bigEndian) {
        return toBin(dec, bigEndian, new byte[8]);
    }

    private static byte[] toBin(long dec, boolean bigEndian, byte[] out) {
        for (int i = 0; i < out.length; ++i) {
            int shift = (bigEndian ? out.length - 1 - i : i) * 8;

            out[i] = (byte) (dec >>> shift);
        }

        return out;
    }

    public static long bcdToLong(byte[] bcd) {
        int end = bcd.length;

        // Ignore trailing 0xff padding
        while (end > 0 && bcd[end - 1] == (byte) 0xff) {
            --end;
        }

        long out = 0;

        for (int i = 0; i < end; ++i) {
            int digit1 = (bcd[i] >> 4) & 15;
            int digit2 = bcd[i] & 15;

            if (digit1 > 10) {
                throw new IllegalArgumentException("Invalid BCD digit: " + Integer.toHexString(digit1));
            }

            out = out * 10 + digit1;

            if (digit2 == 0xf && i == end - 1) {
                // Last digit processed
                break;
            }
            else if (digit2 > 10) {
                throw new IllegalArgumentException("Invalid BCD digit: " + Integer.toHexString(digit1));
            }

            out = out * 10 + digit2;
        }

        return out;
    }

    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = java.util.Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static byte[] xor(byte[] first, byte[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException("Array sizes differ");
        }

        byte[] result = new byte[first.length];

        for (int i = 0; i < first.length; ++i) {
            result[i] = (byte) (first[i] ^ second[i]);
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
            if (offsetDigits != 0) {
                sb.append(String.format(offsetFormat, i + startOffset));
            }

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
