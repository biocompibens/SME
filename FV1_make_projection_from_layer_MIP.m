function zprojf1=FV1_make_projection_from_layer_MIP(Img3,zmap,up,low) 

Img3=uint16(65535*mat2gray(Img3));

Img3=cat(3,uint16(zeros(size(Img3,1),size(Img3,2),up)),uint16(Img3),uint16(zeros(size(Img3,1),size(Img3,2),low)));

%    zprojf1=uint8(uint8(zeros(size(Img1,1),size(Img1,2))));
zprojf11=[];
for int=1:up+low+1
   zprojf11(:,:,int)=uint16(zeros(size(Img3,1),size(Img3,2)));
end
%               zprojf20=uint16(zeros(size(Img1,1),size(Img1,2)));
 temp=[];  
   for kk=min(zmap(:)):max(zmap(:))
       for int=1:up+low+1
             temp=Img3(:,:,kk+int-1);
             zprojf11t=zprojf11(:,:,int);
              zprojf11t(zmap==kk)=temp(zmap==kk);
              zprojf11(:,:,int)=zprojf11t;
       end
          kk 
   end
  
    [zprojf1,zmap1]=max(zprojf11,[],3); 