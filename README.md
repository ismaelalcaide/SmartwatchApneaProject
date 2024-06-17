# Detección de Apnea del Sueño con Smartwatch y ML

Este repositorio forma parte de un proyecto de investigación enfocado en el análisis de las apneas del sueño. Contiene diferentes herramientas y aplicaciones desarrolladas para la conversión y procesamiento de datos, así como para la implementación de algoritmos de aprendizaje automático con el objetivo de detectar y clasificar eventos de apnea del sueño.

## Contenidos del Repositorio

### 1. ApneaDatabase-1-minute-
En esta carpeta se encuentran varios archivos Python destinados a convertir archivos EDF y RML de datos de pacientes de estudios de apnea en archivos CSV legibles. A continuación, se detalla cada archivo:

- annotate.py: Este script se encarga de anotar los datos de apnea según las categorías establecidas.
- extract_data.py: Este script extrae datos de los archivos EDF y RML, y los convierte a CSV.
- extract_data_con_ruido.py: Similar a extract_data.py, pero incluye manejo de ruido gaussiano en los datos durante la extracción.
- README.md: El archivo README.md dentro de esta carpeta proporciona información sobre la base de datos utilizada para los estudios de apnea, incluyendo enlaces a los recursos de la base de datos y una explicación de las diferentes categorías de apnea.

### 2. MultisensorTracking
Esta carpeta contiene una aplicación con una interfaz mejorada para proporcionar una mejor experiencia al usuario. La aplicación es legible y está estructurada de manera eficiente, facilitando su uso y comprensión.

### 3. Procesamiento_Datos
En esta carpeta se encuentra el archivo algoritmosAA.py que contiene todos los algoritmos de aprendizaje automático utilizados en el proyecto. A continuación se detalla cada modelo y su propósito:

#### Modelos de Aprendizaje Automatico
- Random Forest: Un modelo de ensamble que utiliza múltiples árboles de decisión para mejorar la precisión y reducir el sobreajuste.
- Support Vector Machine (SVM): Un modelo de clasificación que encuentra el hiperplano que maximiza la separación entre las clases.
- Multilayer Perceptron (MLP): Una red neuronal feedforward que se entrena utilizando backpropagation.
- Logistic Regression: Un modelo lineal utilizado para clasificación binaria.
- Convolutional Neural Network (CNN) + Long Short-Term Memory (LSTM): Un modelo híbrido que combina capas convolucionales para extracción de características y capas LSTM para captura de dependencias temporales.
- XGBoost + Random Forest: Un modelo de ensamble que combina un clasificador XGBoost y un Random Forest utilizando VotingClassifier para mejorar la precisión.

#### Gráficas y Evaluaciones
- Gráfica t-SNE: Utilizada para la visualización de datos de alta dimensionalidad en dos dimensiones.
- Gini Impurity: Utilizada para medir la importancia de las características en el modelo Random Forest.
