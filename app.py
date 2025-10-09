from flask import Flask, request, render_template, jsonify
import pandas as pd
import numpy as np
from io import StringIO

app = Flask(__name__)

# Helper to convert dates to month index
def months_index(dates):
    base = dates.min()
    return ((dates.dt.year - base.year) * 12 + (dates.dt.month - base.month)).astype(int)

# Simple linear regression
def linear_regression(x, y):
    X = np.vstack([np.ones_like(x, dtype=float), x.astype(float)]).T
    theta = np.linalg.pinv(X.T @ X) @ X.T @ y
    return float(theta[0]), float(theta[1])

# Forecasting function
def forecast_from_df(df, months_ahead=3):
    df['date'] = pd.to_datetime(df['date'])
    df = df.sort_values('date')
    x = months_index(df['date']).to_numpy()
    y = df['bill'].to_numpy().astype(float)

    intercept, slope = linear_regression(x, y)
    last_date = df['date'].max()
    last_x = x.max()

    preds = []
    for i in range(1, months_ahead+1):
        xi = last_x + i
        pred = intercept + slope * xi
        when = (last_date + pd.DateOffset(months=i)).strftime("%Y-%m-%d")
        preds.append({"date": when, "predictedBill": round(float(pred), 2)})

    history = [{"date": d.strftime("%Y-%m-%d"), "bill": float(b)} for d, b in zip(df['date'], df['bill'])]
    return {"history": history, "forecast": preds}

# Routes
@app.route('/')
def index():
    return render_template("index.html")

@app.route('/api/upload', methods=['POST'])
def upload():
    file = request.files['file']
    months = int(request.form.get('months', 3))
    text = file.read().decode("utf-8")
    df = pd.read_csv(StringIO(text))
    df.columns = [c.lower() for c in df.columns]
    if "date" not in df.columns or "bill" not in df.columns:
        df = df.iloc[:, :2]
        df.columns = ["date", "bill"]
    return jsonify(forecast_from_df(df, months))

if __name__ == "__main__":
    app.run(debug=True)
