package com.example.calculator;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import android.app.TabActivity;

public class MainActivity extends TabActivity {
    private TextView txtExpression;
    private TextView txtResult;
    private List<Integer> checkList; // -1: 이콜, 0: 연산자, 1: 숫자, 2: . / 예외 발생을 막는 리스트
    private List<Integer> slashlist; //()위치 파악을 위한 리스트
    private List<String> tmplist; //()위치 저장을 위한 리스트
    private Stack<String> operatorStack; // 연산자를 위한 스택
    private List<String> infix; // 중위 표기
    private List<String> infix2; // 중위 표기
    private List<String> infix3; // 중위 표기
    private List<String> postfix; // 후위 표기
    private Stack<String> operatorStackslash; // 괄호 연산자를 위한 스택
    private List<String> postfixslash; // 괄호 후위 표기
    private List<String> tmp;
    private boolean check;
    public ArrayList<String> items;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.init();

        TabHost tabHost = getTabHost();

        TabHost.TabSpec tabSpecSong = tabHost.newTabSpec("SONG").setIndicator("계산기");
        tabSpecSong.setContent(R.id.tabSong);
        tabHost.addTab(tabSpecSong);

        TabHost.TabSpec tabSpecArtist = tabHost.newTabSpec("ARTIST").setIndicator("HISTORY");
        tabSpecArtist.setContent(R.id.tabArtist);
        tabHost.addTab(tabSpecArtist);
        tabHost.setCurrentTab(0);

        history();

    }

    void history(){

        // ArrayAdapter 생성. 아이템 View를 선택(single choice)가능하도록 만듦.
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, items) ;

        // listview 생성 및 adapter 지정.
        ListView listview = (ListView) findViewById(R.id.listview1) ;
        listview.setAdapter(adapter) ;



        // delete button에 대한 이벤트 처리.
        Button deleteButton = (Button)findViewById(R.id.delete) ;
        deleteButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                int count, checked ;
                count = adapter.getCount() ;

                if (count > 0) {
                    // 현재 선택된 아이템의 position 획득.
                    checked = listview.getCheckedItemPosition();

                    if (checked > -1 && checked < count) {
                        // 아이템 삭제
                        items.remove(checked) ;

                        // listview 선택 초기화.
                        listview.clearChoices();

                        // listview 갱신.
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }) ;

    }

    // 필드 초기화
    void init() {
        txtExpression = findViewById(R.id.txt_expression);
        txtResult = findViewById(R.id.txt_result);
        checkList = new ArrayList<>();
        slashlist= new ArrayList<>();
        tmplist= new ArrayList<String>();
        operatorStack = new Stack<>();
        infix = new ArrayList<>();
        infix2 = new ArrayList<>();
        infix3 = new ArrayList<>();
        postfix = new ArrayList<>();
        operatorStackslash = new Stack<>();
        postfixslash = new ArrayList<>();
        tmp = new ArrayList<>();
        check = true;
        items = new ArrayList<String>() ;

        // ActionBar actionBar = getSupportActionBar(); //위 액션바 제거
        //assert actionBar != null;
        //actionBar.hide();
    }

    // 숫자, 연산자 버튼 이벤트 처리
    public void buttonClick(View v) {
        if (!checkList.isEmpty() && checkList.get(checkList.size() - 1) == -1) { //체크리스트의 값이
            txtExpression.setText(txtResult.getText().toString());
            checkList.clear();
            checkList.add(1); // 정수
            checkList.add(2); // .
            checkList.add(1); // 소수점
            txtResult.setText("");
        }
        switch (v.getId()) {
            case R.id.btn_1: addNumber("1");break;
            case R.id.btn_2: addNumber("2");break;
            case R.id.btn_3: addNumber("3");break;
            case R.id.btn_4: addNumber("4");break;
            case R.id.btn_5: addNumber("5");break;
            case R.id.btn_6: addNumber("6");break;
            case R.id.btn_7: addNumber("7");break;
            case R.id.btn_8: addNumber("8");break;
            case R.id.btn_9: addNumber("9");break;
            case R.id.btn_0: addNumber("0");break;
            case R.id.btn_dot: addDot(".");break;
            case R.id.btn_div: addOperator("/");break;
            case R.id.btn_mod: addOperator("%");break;
            case R.id.btn_mul: addOperator("X");break;
            case R.id.btn_plus: addOperator("+");break;
            case R.id.btn_minus: addOperator("-");break;
            case R.id.btn_AND: addOperator("AND");break;
            case R.id.btn_OR: addOperator("OR");break;
            case R.id.btn_XOR: addOperator("XOR");break;
            case R.id.openslash: addslash("(");break;
            case R.id.closeslash: addslash(")");break;
            case R.id.btn_NOT: addNOT("NOT");break;
        }
    }

    // 클리어 버튼 이벤트 처리
    public void clearClick(View v) {
        infix.clear();
        checkList.clear();
        txtExpression.setText("");
        txtResult.setText("");
        operatorStack.clear();
        postfix.clear();
    }

    // 지우기 버튼 이벤트 처리
    public void deleteClick(View v) {
        if (txtExpression.length() != 0) {
            checkList.remove(checkList.size() - 1);
            String[] ex = txtExpression.getText().toString().split(" ");
            List<String> li = new ArrayList<String>();
            Collections.addAll(li, ex);
            li.remove(li.size() - 1);
            // 마지막이 연산자일 때 " " 빈칸 추가
            if (li.size() > 0 && !isNumber(li.get(li.size() - 1)))
                li.add(li.remove(li.size() - 1) + " ");
            txtExpression.setText(TextUtils.join(" ", li));
        }
        txtResult.setText("");
    }

    // 숫자 버튼 이벤트 처리
    void addNumber(String str) {
        if(checkList.size()==0){
            checkList.add(1); // 숫자가 들어왔는지 체크리스트에 표시
            txtExpression.append(str); // UI
            infix.add(str);
        }
        else{
            if(checkList.get(checkList.size()-1)==1 || checkList.get(checkList.size()-1)==2){
                checkList.add(1); // 숫자가 들어왔는지 체크리스트에 표시
                String tmp = infix.remove(infix.size()-1);
                txtExpression.append(str); // UI
                infix.add(tmp+str);
            }
            else{
                checkList.add(1); // 숫자가 들어왔는지 체크리스트에 표시
                txtExpression.append(str); // UI
                infix.add(str);
            }
        }
    }

    // . 버튼 이벤트 처리
    void addDot(String str) {
        if (checkList.isEmpty()) {
            Toast.makeText(getApplicationContext(), ". 을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else if (checkList.get(checkList.size() - 1) != 1) {
            Toast.makeText(getApplicationContext(), ". 을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        // 하나의 수에 . 이 여러 개 오는 것을 막기
        for (int i = checkList.size() - 2; i >= 0; i--) {
            int check = checkList.get(i);
            if (check == 2) {
                Toast.makeText(getApplicationContext(), ". 을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (check == 0) break;
            if (check == 1) continue;
        }
        checkList.add(2);
        String tmp = infix.remove(infix.size()-1);
        txtExpression.append(str); // UI
        infix.add(tmp+str);
    }

    // 연산자 버튼 이벤트 처리
    void addOperator(String str) {
        try {
            if (checkList.isEmpty()) { // 처음 연산자 사용 막기
                Toast.makeText(getApplicationContext(), "연산자가 올 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            } else if (checkList.get(checkList.size() - 1) == 0 && checkList.get(checkList.size() - 1) == 2) { // 연산자 두 번 사용, 완벽한 수가 오지 않았을 때 막기
                Toast.makeText(getApplicationContext(), "연산자가 올 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            checkList.add(0);
            infix.add(str);
            txtExpression.append(" " + str + " ");
        } catch (Exception e) {
            Log.e("addOperator", e.toString());
        }

    }
    // 괄호 이벤트 처리
    void addslash(String str) {
        checkList.add(0);
        infix.add(str);
        txtExpression.append(" " + str + " ");
    }

    // 연산자 NOT 이벤트 처리
    void addNOT(String str) {
        if(checkList.contains(0)){
            Toast.makeText(getApplicationContext(), "연산자가 올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        checkList.add(0);
        txtExpression.append(str + " ");
        infix.add(str);
    }

    //파일에서 입력받기
    public void addinput(View v){
        check=false;
        StringBuffer strBuffer = new StringBuffer();
        try {
            FileInputStream is = openFileInput("ex.txt");
            System.out.println(is);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line="";
            while((line=reader.readLine())!=null){
                strBuffer.append(line+"\n");
            }
            System.out.println(strBuffer);
            txtExpression.setText(strBuffer);
            reader.close();
            is.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "파일 없음" , Toast.LENGTH_SHORT).show();
        }
    }
    //파일에 저장하기
    public void resultclick(View v){
        try {
            FileOutputStream outFs = openFileOutput("file2.txt", Context.MODE_PRIVATE);
            String str = txtExpression.getText() + " = " + txtResult.getText();
            System.out.println(str);
            outFs.write(str.getBytes());
            outFs.close();
            Toast.makeText(getApplicationContext(), "file.txt가 생성됨", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
        }
    }
    //히스토리 파일에 저장하기
    public void savefile(View v){
        try {
            System.out.println("처리됨");
            FileOutputStream outFs = openFileOutput("savefile.txt", Context.MODE_PRIVATE);
            for (String use:items){
                System.out.println(use);
                outFs.write(use.getBytes());
                outFs.write('\n');
            }
            outFs.close();
            Toast.makeText(getApplicationContext(), "savefile.txt가 생성됨", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
        }
    }


    // 이콜 버튼 이벤트 처리
    public void equalClick(View v) {
        if(check==true){
            if (txtExpression.length() == 0) return; //식에 값이 없으면 리턴
            if (checkList.get(checkList.size() - 1) != 1) {
                Toast.makeText(getApplicationContext(), "숫자를 제대로 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            System.out.println("???:" + infix);
            checkList.add(-1);
            int check = 0;

            if(infix.contains("AND") || infix.contains("OR") || infix.contains("XOR")) check = 1;
            else if (infix.contains("NOT")) check = 2;

            if(check == 1) result2();
            else if (check ==2) resultnot();
            else result();
        }
        else{
            Collections.addAll(infix, txtExpression.getText().toString().split(" "));
            System.out.println("???:" + infix);
            result();
        }

    }

    // 연산자 가중치 (우선순위 *,/,%,+,-)
    int getWeight(String operator) {
        int weight = 0;
        switch (operator) {
            case "X": case "/":
                weight = 5; break;
            case "%":
                weight = 3; break;
            case "+": case "-":
                weight = 1; break;
        }
        return weight;
    }

    // 비트연산자 가중치 (우선순위 *,/,%,+,-)
    int getWeight2(String operator) {
        int weight = 0;
        switch (operator) {
            case "AND":
                weight = 5; break;
            case "XOR":
                weight = 3; break;
            case "OR":
                weight = 1; break;
        }
        return weight;
    }

    // 숫자 판별
    boolean isNumber(String str) {
        boolean result = true;
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException e) {
            result = false;
        }
        return result;
    }

    // 전위 -> 후위 일반 연산용
    void infixToPostfix() {
        for (String item : infix) {
            // 피연산자
            if (isNumber(item)) postfix.add(String.valueOf(Double.parseDouble(item)));
                // 연산자
            else {
                if (operatorStack.isEmpty()) operatorStack.push(item);
                else {
                    if (getWeight(operatorStack.peek()) >= getWeight(item)) postfix.add(operatorStack.pop());
                    operatorStack.push(item);
                }
            }
        }
        while (!operatorStack.isEmpty()) postfix.add(operatorStack.pop());
    }
    // 전위 -> 후위 괄호 계산 일반 연산용
    void infixToPostfixslash() {
        for (String item : tmp) {
            // 피연산자
            if (isNumber(item)) postfixslash.add(String.valueOf(Double.parseDouble(item)));
                // 연산자
            else {
                if (operatorStackslash.isEmpty()) operatorStackslash.push(item);
                else {
                    if (getWeight(operatorStackslash.peek()) >= getWeight(item)) postfixslash.add(operatorStackslash.pop());
                    operatorStackslash.push(item);
                }
            }
        }
        while (!operatorStackslash.isEmpty()) postfixslash.add(operatorStackslash.pop());
    }
    // 전위 -> 후위  비트연산용
    void infixToPostfix2() {
        for (String item : infix) {
            // 피연산자
            if (isNumber(item)) postfix.add(String.valueOf(Byte.parseByte(item)));
                // 연산자
            else {
                if (operatorStack.isEmpty()) operatorStack.push(item);
                else {
                    if (getWeight(operatorStack.peek()) >= getWeight(item)) postfix.add(operatorStack.pop());
                    operatorStack.push(item);
                }
            }
        }
        while (!operatorStack.isEmpty()) postfix.add(operatorStack.pop());
    }

    // 일반 계산식
    String calculate(String num1, String num2, String op) {
        double first = Double.parseDouble(num1);
        double second = Double.parseDouble(num2);
        double result = 0.0;
        try {
            switch (op) {
                case "X": result = first * second;break;
                case "/": result = first / second;break;
                case "%": result = first % second;break;
                case "+": result = first + second;break;
                case "-": result = first - second;break;
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "연산할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
        return String.valueOf(result);
    }

    // 괄호 값 계산 처리
    void slashdelete() {
        int x = 0;
        int k = 0;

        for (int i = 0; i < infix.size(); i++) { //괄호 체크
            String item = infix.get(i);
            if (item.equals("(")) { slashlist.add(i); }
            if (item.equals(")")) { slashlist.add(i); }
        }
        System.out.println(slashlist);

        for (int i=0; i<slashlist.size()/2; i++){ // 괄호 안의 값 구해서 tmplist에 저장
            for (int j=slashlist.get(x)+1; j<slashlist.get(x+1); j++){
                tmp.add(infix.get(j));
            }
            System.out.println(tmp);
            infixToPostfixslash();
            System.out.println(postfixslash);

            while (postfixslash.size() != 1) {
                if (!isNumber(postfixslash.get(k))) {
                    postfixslash.add(k - 2, calculate(postfixslash.remove(k - 2), postfixslash.remove(k - 2), postfixslash.remove(k - 2)));
                    k = -1;
                }
                k++;
            }
            tmplist.add(postfixslash.remove(0));
            tmp.clear();
            x=x+2;
        }
        System.out.println(infix);
        System.out.println(tmplist);
        System.out.println("infix2: " + infix2);
        System.out.println("infix: " + infix);

        int z1=0;
        //리스트의 개수만큼 번호로 i for문 작동
        //i가 tmplist 범위안에 들면 패스 안들면 삽입
        for (int i=0; i<infix.size();i++){
            int z=0;
            boolean check= true;

            for (int j=0; j<slashlist.size()/2; j++){
                if (i == slashlist.get(z)) {
                    infix2.add(tmplist.get(z1));
                    z1=z1+1;
                    check=false;
                    break;
                }
                for(int q= slashlist.get(z); q<slashlist.get(z+1)+1; q++){
                    if(i==q) check=false;
                }
                z=z+2;
            }
            if(check==true) infix2.add(infix.get(i));
        }

        System.out.println("최종 infix2 값: " + infix2);
        infix=infix2;
        System.out.println("최종 infix 값: " + infix);
        System.out.println("최종 infix 값: " + infix.size());
    }

    // 비트 계산용
    String calculate2(String num1, String num2, String op) {
        byte first = Byte.parseByte(num1);
        byte second = Byte.parseByte(num2);
        byte result = 0;
        try {
            switch (op) {
                case "AND": result = (byte) (first & second); break;
                case "XOR": result = (byte) (first ^ second); break;
                case "OR": result = (byte) (first ^ second); break;
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "연산할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
        return String.valueOf(result);
    }

    // 일반연산 최종 결과
    void result() {
        for (int i = 0; i < infix.size(); i++) { //괄호 체크
            String item = infix.get(i);
            if (item.equals("(")) { slashdelete(); break; }
        }
        System.out.println("check2");
        int i = 0;
        slashlist.clear();

        infixToPostfix();
        while (postfix.size() != 1) {
            if (!isNumber(postfix.get(i))) {
                postfix.add(i - 2, calculate(postfix.remove(i - 2), postfix.remove(i - 2), postfix.remove(i - 2)));
                i = -1;
            }
            i++;
        }
        txtResult.setText(postfix.remove(0)); //결과값 출력
        items.add(txtExpression.getText() + " = " + txtResult.getText()); //히스토리추가
        infix.clear();
        infix2.clear();

    }
    // 비트연산 최종 결과
    void result2(){
        int i = 0;
        infixToPostfix2();
        while (postfix.size() != 1) {
            if (!isNumber(postfix.get(i))) {
                postfix.add(i - 2, calculate2(postfix.remove(i - 2), postfix.remove(i - 2), postfix.remove(i - 2)));
                i = -1;
            }
            i++;
        }
        txtResult.setText(postfix.remove(0));//결과값 출력
        items.add(txtExpression.getText() + " = " + txtResult.getText()); //히스토리추가
        infix.clear();
        infix2.clear();
    }
    // NOT 연산 최종 결과
    void resultnot(){
        byte tmp = Byte.parseByte(infix.get(1));
        int res = ~tmp;
        txtResult.setText(String.valueOf(res));
        items.add(txtExpression.getText() + " = " + txtResult.getText()); //히스토리추가
    }
}