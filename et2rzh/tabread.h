#define rows 224
#define cols   5

int tabread(int n, float arr[][n], char fname[]){
    
    FILE *file;
    char *buffer;
    int  ret,row=0,i,j,sl,ch;
    
    // Field Separator
    char delims[]=" \t";
    char *result=NULL;
    
    // Memory allocation
    double **mat = malloc( rows*sizeof(double*));
    for(i = 0; i < rows; i++)
    mat[i] = malloc( cols*sizeof(double));
    
    if ((file = fopen(fname, "r")) == NULL){
        fprintf(stdout, "Error: Can't open file !\n");
        return -1;
    }
    while(!feof(file))
    {
        buffer = malloc(sizeof(char) * 4096);
        memset(buffer, 0, 4096);
        ret = fscanf(file, "%4095[^\n]\n", buffer);
        if (ret != EOF) {
            int field = 0;
            result = strtok(buffer,delims);
            while(result!=NULL){
                // Set no of fields according to your requirement
                if(field>cols)break;
                mat[row][field]=atof(result);
                result=strtok(NULL,delims);
                field++;
            }
            ++row;
        }
        free(buffer);
    }
    fclose(file);
 
    for(i=0;i<rows;i++){
      sl=(int)mat[i][0];
      ch=(int)mat[i][1];
      arr[sl][ch]=mat[i][2];
      free(mat[i]);
    }
    free(mat);
}
