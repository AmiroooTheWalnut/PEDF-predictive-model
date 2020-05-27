clc
clear
[~,~,rawData]=xlsread('roadTrafficFinesLogANN_LATEST_50000_-1_noNormalization.csv');

processed=cell(1,size(rawData,2));

caseCounter=1;
rowCounter=1;
for i=1:size(rawData,1)
    if rowCounter<=13
        for column=1:size(rawData,2)
            if rawData{i,column}==1
%                 disp('start')
                processed{caseCounter,column}=rowCounter;
            end
        end
    end
    rowCounter=rowCounter+1;
    if strcmp(rawData{i,1},'***') 
        rowCounter=1;
        caseCounter=caseCounter+1;
    end
    
end

disp('Nothing')

fid = fopen('roadTrafficFinesLogSPMF_DATA_50000.csv','w'); 

for row=1:size(processed,1)
    rowStr='';
    for col=1:size(processed,2)
        if processed{row,col}==13
            rowStr=[rowStr,' ',num2str(processed{row,col}),' ','-2'];
        elseif isempty(processed{row,col})==0
            if col==1
                rowStr=[rowStr,num2str(processed{row,col})];
                rowStr=[rowStr,' ','-1'];
            else
                rowStr=[rowStr,' ',num2str(processed{row,col})];
                rowStr=[rowStr,' ','-1'];
            end
        end
    end
    fprintf(fid, '%s\n',rowStr);
end
fclose(fid);