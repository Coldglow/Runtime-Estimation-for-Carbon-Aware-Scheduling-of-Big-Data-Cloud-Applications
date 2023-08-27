import openpyxl
import csv


def convert_xlsx_to_csv(input_file, output_file):
    # 使用 openpyxl 读取 .xlsx 文件
    wb = openpyxl.load_workbook(input_file)
    sheet = wb.active

    # 使用 csv 模块保存数据到 .csv 文件
    with open(output_file, 'w', newline='', encoding='utf-8') as csvfile:
        csv_writer = csv.writer(csvfile)
        for row in sheet.iter_rows(values_only=True):
            csv_writer.writerow(row)


if __name__ == "__main__":
    input_excel_file = "../data/KMeans.xlsx"
    output_excel_file = "../data/KMeans.csv"

    convert_xlsx_to_csv(input_excel_file, output_excel_file)
