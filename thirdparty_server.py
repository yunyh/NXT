# -*- coding: utf-8 -*-
import sqlite3
from flask import Flask, jsonify, request, g
from gcm import *

DATABASE = '/home/dbsdudguq/dev/nxt/db_2.db'
TABLE_DEVICEID = 'DeviceID'
NXT = 'NXT'
CLIENT = 'Client'
app = Flask(__name__)
app.secret_key = 'AG!@S#(*EVN&%D08182VDf3_#$@+*#'
gcm = GCM("AIzaSyBAlqjhRMSMaAvco3BihwjopLbObQo5TWE")
sendMessage = {'message': 'message', 'param2': 'value2'}

def get_db():
    db = getattr(g, '_database', None)
    if db is None:
        db = g._database = sqlite3.connect(DATABASE)
    return db

@app.teardown_appcontext
def close_connection(exception):
    db = getattr(g, '_database', None)
    if db is not None:
        db.close()

@app.route('/')
def index():
    return 'Safety Login'

#GCM ID 등록 함수
@app.route('/api/user/gcm/<devicetype>', methods=['PUT'])
def registration_id(devicetype):
    id = request.form['id']
    cur = get_db().cursor()
    if devicetype is not None:
        cur.execute("SELECT * FROM " + TABLE_DEVICEID + " WHERE d_id = ? AND d_type = ?",
                    (id, devicetype))
        data = cur.fetchone()
        print(id + " : " + devicetype)
        if data is None:
            cur.execute("INSERT INTO " + TABLE_DEVICEID + " ('d_id', 'd_type') VALUES (?,?) ", (id, devicetype))
            get_db().commit()
            return jsonify({'message': u'등록 완료'}), 200
        else:
            return jsonify({'message': u'등록된 기기'}), 201
    else:
        return jsonify({'message' : u'등록 실패'}), 401

#경고 확인하고 NXT에서 다시 감시 시작을 보내는 함수
@app.route('/api/user/confirm', methods=['POST'])
def confirm_alert():
    status = request.form['status']
    cur = get_db().cursor()
    if status is not None:
        print(status)
        sendMessage['message'] = status
        cur.execute("SELECT d_id FROM " + TABLE_DEVICEID + " WHERE d_type = '" + NXT +"'")
        data = cur.fetchall()
        list = []
        for row in data:
            list.append(row[0])
        print(list)
        json.dumps(gcm.json_request(registration_ids=list, data=sendMessage))
        return jsonify({'message': u'경고 해제'}), 200
    else:
        print("error")
        return jsonify({'message': u'서버 에러'}), 401

#경고 보내는 함수
@app.route('/api/user/alert/', methods=['POST'])
def send_gcm():
    status = request.form['alert']
    cur = get_db().cursor()
    if status is not None:
        sendMessage['message'] = status
        cur.execute("SELECT d_id FROM " + TABLE_DEVICEID + " WHERE d_type = '" + CLIENT +"'")
        data = cur.fetchall()
        list = []
        for row in data:
            list.append(row[0])
        print(list)
        json.dumps(gcm.json_request(registration_ids=list, data=sendMessage))
        return jsonify({'message': u'경고 알림'}), 200
    else:
        return jsonify({'message': u'서버 에러'}), 401

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)