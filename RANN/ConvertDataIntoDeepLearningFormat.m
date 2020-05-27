clc
clear
%                 no     nu       nu     nu      nu      nu           nu
%                 cases  duration amount payment expense org:resource article
numRowsForEachCase=11+2 +   1    +  1   +   1   +   1   +      1     +   1;
%                                     no               nu        no
%                                     notificationType matricola vehicleClass
numRowsForEachCase=numRowsForEachCase+      2         +    1    +     4;
%                                     nu                 no       no        nu
%                                     totalPaymentAmount lastSent dismissal points
numRowsForEachCase=numRowsForEachCase+      1           +    3   +     26  +  1;
[~,~,rawData]=xlsread('road_traffic_fines_log_PROCESSED_50000.csv');
% load rawData.mat
currentCase=rawData{2,1};
column=3;
caseNumer=1;
startTime=rawData{2,17};
amount=-1;
paymentAmount=-1;
expense=-1;
org_resource=-1;
article=-1;
notificationType=-1;
matricola=-1;
vehicleClass=-1;
totalPaymentAmount=-1;
lastSent=-1;
dismissal=-1;
points=-1;
processedData=cell(1,1,1);
processedData{1,1,caseNumer}='Start';
processedData{1,2,caseNumer}=1;
for i=2:size(rawData,1)
    %no
    processedData{1,1,caseNumer}='Start';
    if strcmp(rawData{i,2},'Create Fine')
        processedData{2,1,caseNumer}='Create Fine';
        processedData{2,column,caseNumer}=1;
    elseif strcmp(rawData{i,2},'Send Fine')
        processedData{3,1,caseNumer}='Send Fine';
        processedData{3,column,caseNumer}=1;
    elseif strcmp(rawData{i,2},'Insert Fine Notification')
        processedData{4,1,caseNumer}='Insert Fine Notification';
        processedData{4,column,caseNumer}=1;
    elseif strcmp(rawData{i,2},'Add penalty')
        processedData{5,1,caseNumer}='Add penalty';
        processedData{5,column,caseNumer}=1;
    elseif strcmp(rawData{i,2},'Send for Credit Collection')
        processedData{6,1,caseNumer}='Send for Credit Collection';
        processedData{6,column,caseNumer}=1;
    elseif strcmp(rawData{i,2},'Payment')
        processedData{7,1,caseNumer}='Payment';
        processedData{7,column,caseNumer}=1;
    elseif strcmp(rawData{i,2},'Insert Date Appeal to Prefecture')
        processedData{8,1,caseNumer}='Insert Date Appeal to Prefecture';
        processedData{8,column,caseNumer}=1;
    elseif strcmp(rawData{i,2},'Send Appeal to Prefecture')
        processedData{9,1,caseNumer}='Send Appeal to Prefectur';
        processedData{9,column,caseNumer}=1;
    elseif strcmp(rawData{i,2},'Receive Result Appeal from Prefecture')
        processedData{10,1,caseNumer}='Receive Result Appeal from Prefecture';
        processedData{10,column,caseNumer}=1;
    elseif strcmp(rawData{i,2},'Notify Result Appeal to Offender')
        processedData{11,1,caseNumer}='Notify Result Appeal to Offender';
        processedData{11,column,caseNumer}=1;
    elseif strcmp(rawData{i,2},'Appeal to Judge')
        processedData{12,1,caseNumer}='Appeal to Judge';
        processedData{12,column,caseNumer}=1;
    end
    processedData{13,1,caseNumer}='End';
    
    %nu
    processedData{14,1,caseNumer}='Duration';
    processedData{14,column,caseNumer}=rawData{i,17}-startTime;
    
    %nu
    if isnan(rawData{i,5})==false
        amount=rawData{i,5};
    else
        amount=-1;
    end
    processedData{15,1,caseNumer}='amount';
    processedData{15,column,caseNumer}=amount;
    
    %nu
    if isnan(rawData{i,6})==false
        paymentAmount=rawData{i,6};
    else
        paymentAmount=-1;
    end
    processedData{16,1,caseNumer}='payment';
    processedData{16,column,caseNumer}=paymentAmount;
    
    %nu
    if isnan(rawData{i,7})==false
        expense=rawData{i,7};
    else
        expense=-1;
    end
    processedData{17,1,caseNumer}='expense';
    processedData{17,column,caseNumer}=expense;
    
    %nu
    if isnan(rawData{i,8})==false
        org_resource=rawData{i,8};
    else
        org_resource=-1;
    end
    processedData{18,1,caseNumer}='org:resource';
    processedData{18,column,caseNumer}=org_resource;
    
    %nu
    if isnan(rawData{i,9})==false
        article=rawData{i,9};
    else
        article=-1;
    end
    processedData{19,1,caseNumer}='article';
    processedData{19,column,caseNumer}=article;
    
    %no
    if isnan(rawData{i,10})==false
        notificationType=rawData{i,10};
    end
    processedData{20,1,caseNumer}='notificationType_C';
    processedData{21,1,caseNumer}='notificationType_P';
    if strcmp(notificationType,'C')
        processedData{20,column,caseNumer}=1;
    end
    if strcmp(notificationType,'P')
        processedData{21,column,caseNumer}=1;
    end
    
    %nu
    if isnan(rawData{i,11})==false
        matricola=rawData{i,11};
    else
        matricola=-1;
    end
    processedData{22,1,caseNumer}='matricola';
    processedData{22,column,caseNumer}=matricola;
    
    %no
    if isnan(rawData{i,12})==false
        vehicleClass=rawData{i,12};
    end
    processedData{23,1,caseNumer}='vehicleClass_A';
    processedData{24,1,caseNumer}='vehicleClasse_C';
    processedData{25,1,caseNumer}='vehicleClasse_M';
    processedData{26,1,caseNumer}='vehicleClasse_R';
    if strcmp(vehicleClass,'A')
        processedData{23,column,caseNumer}=1;
    end
    if strcmp(vehicleClass,'C')
        processedData{24,column,caseNumer}=1;
    end
    if strcmp(vehicleClass,'M')
        processedData{25,column,caseNumer}=1;
    end
    if strcmp(vehicleClass,'R')
        processedData{26,column,caseNumer}=1;
    end
    
    %nu
    if isnan(rawData{i,13})==false
        totalPaymentAmount=rawData{i,13};
    else
        totalPaymentAmount=-1;
    end
    processedData{27,1,caseNumer}='totalPaymentAmount';
    processedData{27,column,caseNumer}=totalPaymentAmount;
    
    %no
    if isnan(rawData{i,14})==false
        lastSent=rawData{i,14};
    end
    processedData{28,1,caseNumer}='lastSent_C';
    processedData{29,1,caseNumer}='lastSent_N';
    processedData{30,1,caseNumer}='lastSent_P';
    if strcmp(lastSent,'C')
        processedData{28,column,caseNumer}=1;
    end
    if strcmp(lastSent,'N')
        processedData{29,column,caseNumer}=1;
    end
    if strcmp(lastSent,'P')
        processedData{30,column,caseNumer}=1;
    end
    
    %no
    if isnan(rawData{i,15})==false
        dismissal=rawData{i,15};
    end
    processedData{31,1,caseNumer}='dismissal_2';
    processedData{32,1,caseNumer}='dismissal_3';
    processedData{33,1,caseNumer}='dismissal_4';
    processedData{34,1,caseNumer}='dismissal_5';
    processedData{35,1,caseNumer}='dismissal_#';
    processedData{36,1,caseNumer}='dismissal_$';
    processedData{37,1,caseNumer}='dismissal_@';
    processedData{38,1,caseNumer}='dismissal_A';
    processedData{39,1,caseNumer}='dismissal_B';
    processedData{40,1,caseNumer}='dismissal_C';
    processedData{41,1,caseNumer}='dismissal_D';
    processedData{42,1,caseNumer}='dismissal_E';
    processedData{43,1,caseNumer}='dismissal_F';
    processedData{44,1,caseNumer}='dismissal_G';
    processedData{45,1,caseNumer}='dismissal_I';
    processedData{46,1,caseNumer}='dismissal_J';
    processedData{47,1,caseNumer}='dismissal_K';
    processedData{48,1,caseNumer}='dismissal_M';
    processedData{49,1,caseNumer}='dismissal_N';
    processedData{50,1,caseNumer}='dismissal_NIL';
    processedData{51,1,caseNumer}='dismissal_Q';
    processedData{52,1,caseNumer}='dismissal_R';
    processedData{53,1,caseNumer}='dismissal_T';
    processedData{54,1,caseNumer}='dismissal_U';
    processedData{55,1,caseNumer}='dismissal_V';
    processedData{56,1,caseNumer}='dismissal_Z';
    if dismissal==2
        processedData{31,column,caseNumer}=1;
    end
    if dismissal==3
        processedData{32,column,caseNumer}=1;
    end
    if dismissal==4
        processedData{33,column,caseNumer}=1;
    end
    if dismissal==5
        processedData{34,column,caseNumer}=1;
    end
    if strcmp(dismissal,'#')
        processedData{35,column,caseNumer}=1;
    end
    if strcmp(dismissal,'$')
        processedData{36,column,caseNumer}=1;
    end
    if strcmp(dismissal,'@')
        processedData{37,column,caseNumer}=1;
    end
    if strcmp(dismissal,'A')
        processedData{38,column,caseNumer}=1;
    end
    if strcmp(dismissal,'B')
        processedData{39,column,caseNumer}=1;
    end
    if strcmp(dismissal,'C')
        processedData{40,column,caseNumer}=1;
    end
    if strcmp(dismissal,'D')
        processedData{41,column,caseNumer}=1;
    end
    if strcmp(dismissal,'E')
        processedData{42,column,caseNumer}=1;
    end
    if strcmp(dismissal,'F')
        processedData{43,column,caseNumer}=1;
    end
    if strcmp(dismissal,'G')
        processedData{44,column,caseNumer}=1;
    end
    if strcmp(dismissal,'I')
        processedData{45,column,caseNumer}=1;
    end
    if strcmp(dismissal,'J')
        processedData{46,column,caseNumer}=1;
    end
    if strcmp(dismissal,'K')
        processedData{47,column,caseNumer}=1;
    end
    if strcmp(dismissal,'M')
        processedData{48,column,caseNumer}=1;
    end
    if strcmp(dismissal,'N')
        processedData{49,column,caseNumer}=1;
    end
    if strcmp(dismissal,'NIL')
        processedData{50,column,caseNumer}=1;
    end
    if strcmp(dismissal,'Q')
        processedData{51,column,caseNumer}=1;
    end
    if strcmp(dismissal,'R')
        processedData{52,column,caseNumer}=1;
    end
    if strcmp(dismissal,'T')
        processedData{53,column,caseNumer}=1;
    end
    if strcmp(dismissal,'U')
        processedData{54,column,caseNumer}=1;
    end
    if strcmp(dismissal,'V')
        processedData{55,column,caseNumer}=1;
    end
    if strcmp(dismissal,'Z')
        processedData{56,column,caseNumer}=1;
    end
    
    %nu
    if isnan(rawData{i,16})==false
        points=rawData{i,16};
    else
        points=-1;
    end
    processedData{57,1,caseNumer}='points';
    processedData{57,column,caseNumer}=points;
    
    if i+1<size(rawData,1)
        if strcmp(rawData{i+1,1},currentCase)
            column=column+1;
        else
            column=column+1;
            processedData{13,1,caseNumer}='End';
            processedData{13,column,caseNumer}=1;
            currentCase=rawData{i+1,1};
            caseNumer=caseNumer+1;
            processedData{1,1,caseNumer}='Start';
            processedData{1,2,caseNumer}=1;
            column=3;
            startTime=rawData{i+1,17};
            amount=-1;
            paymentAmount=-1;
            expense=-1;
            org_resource=-1;
            article=-1;
            notificationType=-1;
            matricola=-1;
            vehicleClass=-1;
            totalPaymentAmount=-1;
            lastSent=-1;
            dismissal=-1;
            points=-1;
        end
    end
    
end


disp('Nothing')

fid = fopen('roadTrafficFinesLogANN_DATA_50000.csv','w'); 

for i=1:size(processedData,3)
    for row=1:size(processedData,1)
%         if row==15
%             disp('Nothing1')
%         end
        rowStr='';
        for column=2:size(processedData,2)
            if isempty(processedData{row,column,i})==true
                rowStr=strcat(rowStr,'-1');
                if column~=size(processedData,2)
                    rowStr=strcat(rowStr,',');
                end
            else
                rowStr=strcat(rowStr,num2str(processedData{row,column,i}));
                if column~=size(processedData,2)
                    rowStr=strcat(rowStr,',');
                end
            end
        end
%         disp(rowStr)
        fprintf(fid, '%s\n',rowStr);
    end
    fprintf(fid, '***\n');
end
fclose(fid);