import pandas as pd
import os
from sklearn.model_selection import train_test_split, GridSearchCV
from sklearn.metrics import accuracy_score, confusion_matrix, precision_score, roc_curve
from sklearn.preprocessing import StandardScaler
from sklearn.neural_network import MLPClassifier
from sklearn.svm import SVC
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier, VotingClassifier
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv1D, MaxPooling1D, LSTM, Dense
from sklearn.manifold import TSNE
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
from imblearn.over_sampling import SMOTE
from xgboost import XGBClassifier

def load_data():
    # Ruta al directorio donde están almacenados los datos
    os.chdir('APNEA_DATA')
    print("Archivos en el directorio:", os.listdir())
    all_files = []

    # Recorrer los directorios y encontrar todos los archivos CSV
    for subdir in os.listdir():
        subdir_path = os.path.join(os.getcwd(), subdir)
        if os.path.isdir(subdir_path):
            # Iterar sobre cada archivo en el subdirectorio
            for file_name in os.listdir(subdir_path):
                if file_name.endswith('.csv'):
                    file_path = os.path.join(subdir_path, file_name)
                    try:
                        # Leer el archivo CSV y añadirlo a la lista
                        df = pd.read_csv(file_path, delimiter=';')
                        all_files.append(df)
                        print("Datos cargados correctamente para", file_name)
                    except Exception as e:
                        print("Error al leer el archivo:", file_name, "->", e)


    # Concatenar todos los DataFrames en uno solo
    combined_data = pd.concat(all_files, ignore_index=True)
    print(combined_data)

    # Nombres de columnas
    combined_data.columns = ['SuenoS', 'HR', 'SP02', 'MOVIMIENTO', 'TEMP', 'LABEL']

    # Preparar los datos
    X = df.drop('LABEL', axis=1)
    y = df['LABEL']

    label_counts = combined_data['LABEL'].value_counts()
    print(label_counts)

    return X, y


def plot_confusion_matrix(conf_matrix, accuracy, precision, model_name):
    plt.figure(figsize=(8, 6))
    sns.heatmap(conf_matrix, annot=True, fmt='d', cmap='Blues', xticklabels=['No Apnea', 'Apnea'], yticklabels=['No Apnea', 'Apnea'])
    plt.xlabel('Predicted')
    plt.ylabel('Actual')
    plt.title(f'Confusion Matrix for {model_name} (Accuracy: {accuracy:.2f}, Precision: {precision:.2f})')
    plt.savefig(f'confusion_matrix_{model_name.lower()}.png')
    plt.show()


def run_random_forest(X, y):
   # Dividir los datos en conjuntos de entrenamiento y prueba
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=42)

    # Crear el modelo con la semilla aleatoria fijada
    rf_model = RandomForestClassifier(n_estimators=100, random_state=42)

    # Entrenar el modelo
    rf_model.fit(X_train, y_train)

    # Realizar predicciones
    y_pred = rf_model.predict(X_test)

    # Evaluar el modelo
    conf_matrix = confusion_matrix(y_test, y_pred)
    precision = precision_score(y_test, y_pred, average='macro')
    accuracy = accuracy_score(y_test, y_pred)
    
    evaluate_model(y_test, y_pred, 'Random Forest')
    
    # Mostrar los resultados
    plot_confusion_matrix(conf_matrix, accuracy, precision, 'Random Forest')
    

def run_svm(X, y):
    # Dividir los datos en conjuntos de entrenamiento y prueba
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=42)

    # Crear el modelo SVM con la semilla aleatoria fijada
    svm_model = SVC(random_state=42)

    # Entrenar el modelo
    svm_model.fit(X_train, y_train)

    # Realizar predicciones
    y_pred = svm_model.predict(X_test)

    # Evaluar el modelo
    conf_matrix = confusion_matrix(y_test, y_pred)
    precision = precision_score(y_test, y_pred, average='macro')
    accuracy = accuracy_score(y_test, y_pred)
    
    evaluate_model(y_test, y_pred, 'SVM')
    
    # Mostrar los resultados
    plot_confusion_matrix(conf_matrix, accuracy, precision, 'SVM')
    

def run_mlp(X, y):
    # Dividir los datos en conjuntos de entrenamiento y prueba
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=42)

    # Crear y entrenar el modelo MLP
    mlp_model = MLPClassifier(hidden_layer_sizes=(100,), activation='relu', solver='adam', random_state=42, max_iter=300)
    mlp_model.fit(X_train, y_train)

    # Realizar predicciones
    y_pred = mlp_model.predict(X_test)

    # Evaluar el modelo
    conf_matrix = confusion_matrix(y_test, y_pred)
    precision = precision_score(y_test, y_pred, average='macro', zero_division=0)
    accuracy = accuracy_score(y_test, y_pred)
    
    evaluate_model(y_test, y_pred, 'MLP')
    
    # Mostrar los resultados
    plot_confusion_matrix(conf_matrix, accuracy, precision, 'MLP')
    

def run_logistic_regression(X, y):
    # Dividir los datos en conjuntos de entrenamiento y prueba
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # Crear y entrenar el modelo de regresión logística
    logistic_model = LogisticRegression(random_state=42)
    logistic_model.fit(X_train, y_train)

    # Realizar predicciones
    y_pred = logistic_model.predict(X_test)

    # Evaluar el modelo
    conf_matrix = confusion_matrix(y_test, y_pred)
    precision = precision_score(y_test, y_pred, average='macro', zero_division=0)
    accuracy = accuracy_score(y_test, y_pred)
    
    evaluate_model(y_test, y_pred, 'Logistic Regression')
    
    # Mostrar los resultados
    plot_confusion_matrix(conf_matrix, accuracy, precision, 'Logistic Regression')


def run_cnn_lstm(X, y):
   # Normalización de los datos
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    # Reshape de los datos para que se ajusten al modelo CNN + LSTM
    X_scaled = X_scaled.reshape((X_scaled.shape[0], X_scaled.shape[1], 1))

    # Dividir los datos en conjuntos de entrenamiento y prueba
    from sklearn.model_selection import train_test_split
    X_train, X_test, y_train, y_test = train_test_split(X_scaled, y, test_size=0.2, random_state=42)

    # Definición del modelo
    model = Sequential()
    model.add(Conv1D(filters=64, kernel_size=3, activation='relu', input_shape=(X_scaled.shape[1], 1)))
    model.add(MaxPooling1D(pool_size=2))
    model.add(LSTM(100, return_sequences=True))
    model.add(LSTM(100))
    model.add(Dense(1, activation='sigmoid'))  # Para clasificación binaria

    model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])

    # Entrenamiento del modelo
    model.fit(X_train, y_train, epochs=10, batch_size=32, validation_data=(X_test, y_test))

    # Evaluación del modelo
    loss, accuracy = model.evaluate(X_test, y_test)
    print(f'Model Accuracy: {accuracy*100:.2f}%')

    # Predicciones
    y_pred_prob = model.predict(X_test)
    y_pred = (y_pred_prob > 0.5).astype(int)

    # Evaluar el modelo
    conf_matrix = confusion_matrix(y_test, y_pred)
    precision = precision_score(y_test, y_pred, average='macro', zero_division=0)
    
    evaluate_model(y_test, y_pred, 'CNN_LSTM')
    
    # Mostrar los resultados
    plot_confusion_matrix(conf_matrix, accuracy, precision, 'CNN_LSTM')
    
    
def run_xgb_rf(X, y):
    # Dividir los datos en conjuntos de entrenamiento y prueba
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=42)
    
    # Aplicar SMOTE para balancear las clases
    sm = SMOTE(random_state=42)
    X_train_res, y_train_res = sm.fit_resample(X_train, y_train)
    
    # Escalar los datos
    scaler = StandardScaler()
    X_train_res = scaler.fit_transform(X_train_res)
    X_test = scaler.transform(X_test)
    
    # Entrenar un clasificador XGBoost con ajuste de hiperparámetros
    xgb_param_grid = {
        'n_estimators': [100, 200, 300],
        'max_depth': [3, 6, 9],
        'learning_rate': [0.01, 0.1, 0.2],
        'scale_pos_weight': [10, 15, 20]
    }
    xgb_clf = GridSearchCV(XGBClassifier(random_state=42, use_label_encoder=False, eval_metric='logloss'), xgb_param_grid, cv=5, scoring='recall')
    xgb_clf.fit(X_train_res, y_train_res)
    
    # Entrenar un clasificador RandomForest con ajuste de hiperparámetros
    rf_param_grid = {
        'n_estimators': [100, 200, 300],
        'max_depth': [10, 20, None],
        'min_samples_split': [2, 5, 10],
        'min_samples_leaf': [1, 2, 4],
        'class_weight': [{0: 1, 1: 10}, {0: 1, 1: 15}, {0: 1, 1: 20}]
    }
    rf_clf = GridSearchCV(RandomForestClassifier(random_state=42), rf_param_grid, cv=5, scoring='recall')
    rf_clf.fit(X_train_res, y_train_res)
    
    # Combinación de modelos usando VotingClassifier
    ensemble_clf = VotingClassifier(estimators=[('xgb', xgb_clf.best_estimator_), ('rf', rf_clf.best_estimator_)], voting='soft', weights=[2, 1])
    ensemble_clf.fit(X_train_res, y_train_res)
    
    # Predecir las probabilidades
    y_prob = ensemble_clf.predict_proba(X_test)[:, 1]
    
    # Encontrar el mejor umbral usando la curva ROC
    fpr, tpr, thresholds = roc_curve(y_test, y_prob)
    optimal_idx = np.argmax(tpr - fpr)
    optimal_threshold = thresholds[optimal_idx]
    
    # Ajustar el umbral de decisión
    y_pred = (y_prob >= optimal_threshold).astype(int)
    
    # Evaluar el modelo
    conf_matrix = confusion_matrix(y_test, y_pred)
    accuracy = accuracy_score(y_test, y_pred)
    precision = precision_score(y_test, y_pred)

    evaluate_model(y_test, y_pred, 'XGB_RF')
    
    # Mostrar los resultados
    plot_confusion_matrix(conf_matrix, accuracy, precision, 'XGB_RF')
    

def run_plot_tsne(X, y):
    # Reducir la dimensionalidad de los datos a 2D usando t-SNE
    tsne = TSNE(n_components=2, random_state=42)
    X_tsne = tsne.fit_transform(X)

    # Crear un DataFrame con los resultados de t-SNE y las etiquetas
    tsne_df = pd.DataFrame(X_tsne, columns=['TSNE1', 'TSNE2'])
    tsne_df['LABEL'] = y.replace({0: 'No Apnea', 1: 'Apnea'})

    # Generar el gráfico t-SNE
    plt.figure(figsize=(10, 8))
    sns.scatterplot(x='TSNE1', y='TSNE2', hue='LABEL', palette='coolwarm', data=tsne_df)
    plt.title('T-SNE plot del dataset')
    plt.xlabel('T-SNE Component 1')
    plt.ylabel('T-SNE Component 2')
    plt.legend(title='Label', loc='best')
    plt.savefig('tsne_plot.png')
    plt.show()
    
    
def run_gini_impurity(X, y):
    # Dividir los datos en conjuntos de entrenamiento y prueba
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=42)

    # Entrenamiento del modelo de Random Forest
    clf = RandomForestClassifier(n_estimators=100, random_state=42)
    clf.fit(X_train, y_train)

    # Evaluación del modelo
    y_pred = clf.predict(X_test)
    accuracy = accuracy_score(y_test, y_pred)
    print(f"Accuracy: {accuracy}")

    # Importancias de características usando Gini impurity
    importances = clf.feature_importances_
    std = np.std([tree.feature_importances_ for tree in clf.estimators_], axis=0)
    indices = np.argsort(importances)[::-1]

    # Graficar las importancias de las características
    plt.figure(figsize=(10, 6))
    plt.title("Feature importances by Gini Impurity")
    plt.bar(range(X.shape[1]), importances[indices], color="r", yerr=std[indices], align="center")
    plt.xticks(range(X.shape[1]), X.columns[indices])
    plt.xlim([-1, X.shape[1]])
    plt.ylabel("Mean decrease in impurity")
    plt.xlabel("Feature")
    plt.savefig('gini_impurity.png')
    plt.show()


def evaluate_model(y_test, y_pred, model_name):
    conf_matrix = confusion_matrix(y_test, y_pred)

    tn, fp, fn, tp = conf_matrix.ravel()

    # Proporción de falsos positivos
    fp_rate = fp / (fp + tn)
    # Proporción de falsos negativos
    fn_rate = fn / (fn + tp)

    print(f"{model_name} - False Positive Rate: {fp_rate:.2f}, False Negative Rate: {fn_rate:.2f}")



# Cargar y preprocesar los datos
X, y = load_data()


# Ejecutar los modelos
# run_random_forest(X, y)
# run_svm(X, y)
# run_mlp(X, y)
# run_logistic_regression(X, y)
# run_cnn_lstm(X, y)
# run_xgb_rf(X, y)
# run_plot_tsne(X, y)
# run_gini_impurity(X, y)