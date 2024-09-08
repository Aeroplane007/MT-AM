import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Function to read the 17th column from a CSV file
def read_17th_column(file_path):
    df = pd.read_csv(file_path, decimal=',')
    # Assuming the 17th column is the 16th index (0-based index)
    column_17 = (df.iloc[:, 15] - df.iloc[:, 1]) / 1000000
    return column_17

# Function to remove outliers using the IQR method
def remove_outliers(series):
    Q1 = series.quantile(0.25)
    Q3 = series.quantile(0.75)
    IQR = Q3 - Q1
    lower_bound = Q1 - 1.5 * IQR
    upper_bound = Q3 + 1.5 * IQR
    return series[(series >= lower_bound) & (series <= upper_bound)]

object = "A11"

# File paths
file1 = 'csvFiles' + object + '/data'+object+'_5.0_compare_latency.csv'
file2 = 'csvFiles' + object + '/data'+object+'_10.0_compare_latency.csv'
file3 = 'csvFiles' + object + '/data'+object+'_15.0_compare_latency.csv'

# Read the 17th column from each file
column1 = read_17th_column(file1)
column2 = read_17th_column(file2)
column3 = read_17th_column(file3)

# Convert the data to pandas Series, drop any NaN values, ensure numeric type, and remove outliers
column1 = remove_outliers(pd.Series(column1).dropna().astype(float))
column2 = remove_outliers(pd.Series(column2).dropna().astype(float))
column3 = remove_outliers(pd.Series(column3).dropna().astype(float))

print("Throughput = ", len(column1)/(column1.sum()/1000))
print("Throughput = ", len(column2)/(column2.sum()/1000))
print("Throughput = ", len(column3)/(column3.sum()/1000))

# Create a DataFrame for plotting
data = pd.DataFrame({
    'File 1': column1,
    'File 2': column2,
    'File 3': column3
})

# Melt the DataFrame to long format for seaborn
data_melted = data.melt(var_name='Outlier percentage', value_name='Latency[ms]')

# Set the context for larger text
sns.set(context='talk', font_scale=1.2)

# Create the box plots
plt.figure(figsize=(10, 6))
sns.boxplot(x='Outlier percentage', y='Latency[ms]', data=data_melted, palette='Set2', legend=False)

# Customize the x-axis labels
plt.gca().set_xticklabels(['5%', '10%', '15%'])

plt.ylim(7, 25)

# Show the plot with y-axis numbers
plt.show()
