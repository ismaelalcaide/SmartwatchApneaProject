"""Graficas_SleepData.ipynb

Automatically generated by Colab."""

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# Cargar datos
df1 = pd.read_csv('Combined_Sleep_Data.csv')

def calcular_aceleracion_ms2(df):
    factor_conversion = 9.81 / (16383.75 / 4.0)
    df['acx_ms2'] = df[' acx'] * factor_conversion
    df['acy_ms2'] = df[' acy'] * factor_conversion
    df['acz_ms2'] = df[' acz'] * factor_conversion
    df['ac_modulo_ms2'] = np.sqrt(df['acx_ms2']**2 + df['acy_ms2']**2 + df['acz_ms2']**2)
    return df

df1 = calcular_aceleracion_ms2(df1)



"""# Módulo de la Aceleración (ACX, ACY, ACZ)"""

ac_df1 = df1[df1[' sensor '].astype(str).str.contains('A')]

ac_df1_no_zero = ac_df1[ac_df1['ac_modulo_ms2'] != 0]

# Grafico
plt.figure(figsize=(12, 8))
plt.plot(ac_df1_no_zero[' timestamp'], ac_df1_no_zero['ac_modulo_ms2'], marker='o', linestyle='-')
plt.title('Módulo de la Aceleración en función del tiempo')
plt.xlabel('Tiempo')
plt.ylabel('Módulo de la Aceleración')
plt.show()



"""# SleepData_HR.ipynb"""

hr_df1 = df1[df1[' sensor '].astype(str).str.contains('H')]

hr_df1_no_zero = hr_df1[hr_df1[' hr'] != 0]

# Grafico
plt.figure(figsize=(12, 8))
plt.plot(hr_df1_no_zero[' hr'], marker='o', linestyle='-')
plt.title('Heart rate en función del tiempo')
plt.xlabel('Tiempo')
plt.ylabel('HR')
plt.show()



"""# SleepData_IBI.ipynb"""

ibi_df1 = df1[df1[' sensor '].astype(str).str.contains('H')]

ibi_df1_no_zero = ibi_df1[ibi_df1[' ibi'] != 0]

# Grafico
plt.figure(figsize=(12, 8))
plt.plot(ibi_df1_no_zero[' ibi'], marker='o', linestyle='-')
plt.title('Inter Beat Interval en función del tiempo')
plt.xlabel('Tiempo')
plt.ylabel('IBI')
plt.show()



"""# SleepData_SP02.ipynb"""

sp_df1 = df1[df1[' sensor '].astype(str).str.contains('S')]

# Grafico
plt.figure(figsize=(12, 8))
plt.plot(sp_df1[' sop2'], marker='o', linestyle='-')
plt.title('Saturacion de oxigeno en función del tiempo')
plt.xlabel('Tiempo')
plt.ylabel('SP02')
#plt.legend()
plt.show()



"""# SleepData_TEMP.ipynb"""

temp_df1 = df1[df1[' sensor '].astype(str).str.contains('T')]

# Grafico
plt.figure(figsize=(12, 8))
plt.plot(temp_df1[' timestamp'], temp_df1[' temp'], marker='o', linestyle='-')
plt.title('Temperatura en función del tiempo')
plt.xlabel('Tiempo')
plt.ylabel('Temperatura')
plt.show()