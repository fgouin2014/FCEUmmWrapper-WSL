package com.fceumm.wrapper.overlay;

/**
 * **100% RETROARCH NATIF** : Implémentation exacte de input_bits_t
 * 
 * Structure identique à RetroArch officiel :
 * typedef struct {
 *    uint32_t data[8];        // 256 bits (8 * 32)
 *    uint16_t analogs[8];
 *    uint16_t analog_buttons[16];
 * } input_bits_t;
 */
public class RetroArchInputBits {
    
    // **100% RETROARCH NATIF** : Structure identique à RetroArch
    public int[] data = new int[8];        // uint32_t data[8] - 256 bits
    public short[] analogs = new short[8];  // uint16_t analogs[8]
    public short[] analog_buttons = new short[16]; // uint16_t analog_buttons[16]
    
    // **100% RETROARCH NATIF** : Constantes de bits identiques à RetroArch
    public static final int RETRO_DEVICE_ID_JOYPAD_B = 0;
    public static final int RETRO_DEVICE_ID_JOYPAD_Y = 1;
    public static final int RETRO_DEVICE_ID_JOYPAD_SELECT = 2;
    public static final int RETRO_DEVICE_ID_JOYPAD_START = 3;
    public static final int RETRO_DEVICE_ID_JOYPAD_UP = 4;
    public static final int RETRO_DEVICE_ID_JOYPAD_DOWN = 5;
    public static final int RETRO_DEVICE_ID_JOYPAD_LEFT = 6;
    public static final int RETRO_DEVICE_ID_JOYPAD_RIGHT = 7;
    public static final int RETRO_DEVICE_ID_JOYPAD_A = 8;
    public static final int RETRO_DEVICE_ID_JOYPAD_X = 9;
    public static final int RETRO_DEVICE_ID_JOYPAD_L = 10;
    public static final int RETRO_DEVICE_ID_JOYPAD_R = 11;
    public static final int RETRO_DEVICE_ID_JOYPAD_L2 = 12;
    public static final int RETRO_DEVICE_ID_JOYPAD_R2 = 13;
    public static final int RETRO_DEVICE_ID_JOYPAD_L3 = 14;
    public static final int RETRO_DEVICE_ID_JOYPAD_R3 = 15;
    
    public RetroArchInputBits() {
        clear();
    }
    
    /**
     * **100% RETROARCH NATIF** : Clear tous les bits comme RetroArch
     */
    public void clear() {
        for (int i = 0; i < 8; i++) {
            data[i] = 0;
        }
        for (int i = 0; i < 8; i++) {
            analogs[i] = 0;
        }
        for (int i = 0; i < 16; i++) {
            analog_buttons[i] = 0;
        }
    }
    
    /**
     * **100% RETROARCH NATIF** : Set un bit comme RetroArch
     * @param bit_index Index du bit (0-255)
     */
    public void setBit(int bit_index) {
        if (bit_index >= 0 && bit_index < 256) {
            int array_index = bit_index / 32;
            int bit_offset = bit_index % 32;
            data[array_index] |= (1 << bit_offset);
        }
    }
    
    /**
     * **100% RETROARCH NATIF** : Clear un bit comme RetroArch
     * @param bit_index Index du bit (0-255)
     */
    public void clearBit(int bit_index) {
        if (bit_index >= 0 && bit_index < 256) {
            int array_index = bit_index / 32;
            int bit_offset = bit_index % 32;
            data[array_index] &= ~(1 << bit_offset);
        }
    }
    
    /**
     * **100% RETROARCH NATIF** : Test un bit comme RetroArch
     * @param bit_index Index du bit (0-255)
     * @return true si le bit est set
     */
    public boolean isBitSet(int bit_index) {
        if (bit_index >= 0 && bit_index < 256) {
            int array_index = bit_index / 32;
            int bit_offset = bit_index % 32;
            return (data[array_index] & (1 << bit_offset)) != 0;
        }
        return false;
    }
    
    /**
     * **100% RETROARCH NATIF** : Set un bouton joypad comme RetroArch
     * @param device_id RETRO_DEVICE_ID_JOYPAD_*
     */
    public void setJoypadButton(int device_id) {
        if (device_id >= 0 && device_id < 16) {
            setBit(device_id);
        }
    }
    
    /**
     * **100% RETROARCH NATIF** : Clear un bouton joypad comme RetroArch
     * @param device_id RETRO_DEVICE_ID_JOYPAD_*
     */
    public void clearJoypadButton(int device_id) {
        if (device_id >= 0 && device_id < 16) {
            clearBit(device_id);
        }
    }
    
    /**
     * **100% RETROARCH NATIF** : Test un bouton joypad comme RetroArch
     * @param device_id RETRO_DEVICE_ID_JOYPAD_*
     * @return true si le bouton est pressé
     */
    public boolean isJoypadButtonPressed(int device_id) {
        if (device_id >= 0 && device_id < 16) {
            return isBitSet(device_id);
        }
        return false;
    }
    
    /**
     * **100% RETROARCH NATIF** : Set une valeur analogique comme RetroArch
     * @param analog_index Index de l'analog (0-7)
     * @param value Valeur analogique (-32768 à 32767)
     */
    public void setAnalog(int analog_index, short value) {
        if (analog_index >= 0 && analog_index < 8) {
            analogs[analog_index] = value;
        }
    }
    
    /**
     * **100% RETROARCH NATIF** : Get une valeur analogique comme RetroArch
     * @param analog_index Index de l'analog (0-7)
     * @return Valeur analogique
     */
    public short getAnalog(int analog_index) {
        if (analog_index >= 0 && analog_index < 8) {
            return analogs[analog_index];
        }
        return 0;
    }
    
    /**
     * **100% RETROARCH NATIF** : Set une valeur analogique de bouton comme RetroArch
     * @param button_index Index du bouton analogique (0-15)
     * @param value Valeur analogique (0-65535)
     */
    public void setAnalogButton(int button_index, short value) {
        if (button_index >= 0 && button_index < 16) {
            analog_buttons[button_index] = value;
        }
    }
    
    /**
     * **100% RETROARCH NATIF** : Get une valeur analogique de bouton comme RetroArch
     * @param button_index Index du bouton analogique (0-15)
     * @return Valeur analogique
     */
    public short getAnalogButton(int button_index) {
        if (button_index >= 0 && button_index < 16) {
            return analog_buttons[button_index];
        }
        return 0;
    }
    
    /**
     * **100% RETROARCH NATIF** : Copy comme RetroArch
     * @param source Source à copier
     */
    public void copy(RetroArchInputBits source) {
        for (int i = 0; i < 8; i++) {
            data[i] = source.data[i];
        }
        for (int i = 0; i < 8; i++) {
            analogs[i] = source.analogs[i];
        }
        for (int i = 0; i < 16; i++) {
            analog_buttons[i] = source.analog_buttons[i];
        }
    }
    
    /**
     * **100% RETROARCH NATIF** : OR operation comme RetroArch
     * @param other Autre input_bits à OR
     */
    public void or(RetroArchInputBits other) {
        for (int i = 0; i < 8; i++) {
            data[i] |= other.data[i];
        }
    }
    
    /**
     * **100% RETROARCH NATIF** : AND operation comme RetroArch
     * @param other Autre input_bits à AND
     */
    public void and(RetroArchInputBits other) {
        for (int i = 0; i < 8; i++) {
            data[i] &= other.data[i];
        }
    }
    
    /**
     * **100% RETROARCH NATIF** : XOR operation comme RetroArch
     * @param other Autre input_bits à XOR
     */
    public void xor(RetroArchInputBits other) {
        for (int i = 0; i < 8; i++) {
            data[i] ^= other.data[i];
        }
    }
    
    /**
     * **100% RETROARCH NATIF** : Debug string comme RetroArch
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("input_bits_t { ");
        sb.append("data=[");
        for (int i = 0; i < 8; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("0x%08X", data[i]));
        }
        sb.append("], analogs=[");
        for (int i = 0; i < 8; i++) {
            if (i > 0) sb.append(", ");
            sb.append(analogs[i]);
        }
        sb.append("], analog_buttons=[");
        for (int i = 0; i < 16; i++) {
            if (i > 0) sb.append(", ");
            sb.append(analog_buttons[i]);
        }
        sb.append("] }");
        return sb.toString();
    }
}
