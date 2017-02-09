function [OUTput_image,Manifold,Classmap,idmaxini,cost,WW,C1,C2,C3]=Main_SME_method_MIP(Img,nametex2)
% Img=Img1;
%This funtion finds the single manifod in the stack where the object is located. The final output should be the image created from that manifold. The input image should be a stack.

M = [-1 2 -1];%SML operator
[sz1,sz2,sz3]=size(Img);
npxl=sz1*sz2;
timk=double(Img);
% sigma=find_sigma_MIP(Img)
% hG = fspecial('gaussian',[25 25],sigma+0.01); 

%% SML Extraction
               for k=1:size(Img,3)
                   timg=Img(:,:,k);   
%                     timg = imfilter(timg,hG,'symmetric');  
%                       Gx = imfilter(timg, M, 'replicate', 'conv');
%                       Gy = imfilter(timg, M', 'replicate', 'conv');
                      timk(:,:,k) = timg;
               end   
      
%% Fourier transform and kmeans
               class=3;%background, uncertain and foreground
               Norm=2;
               zprof2=reshape(timk,[size(Img,1)*size(Img,2) size(Img,3)]); 
               
            
               
%                  p2 = nextpow2(size(zprof2,2));
%                  hi = 2^p2;
% %
%                  zprof2k=zeros(size(zprof2,1),hi-p2);
%                  zprof2=[zprof2 zprof2k];
%
%

                               tempt=abs(fft(zprof2,size(Img,3),2));


                       tempt(:,[1 ceil(size(Img,3)/2)+1:end])=[];
                       
%                         tempt1=abs(fft(zprof1,size(Img,3),2));
% 
% 
%                        tempt1(:,[1 ceil(size(Img,3)/2)+1:end])=[];
                       
%                      tempt=[tempt tempt1];  
			   
%               tempt=abs(fft(zprof2,size(Img,3),2));

 %              tempt(:,[1 ceil(size(Img,3)/2):end])=[];  
               tempt=tempt./repmat((max(tempt,[],1)-min(tempt,[],1)),[size(tempt,1) 1]);
               [idx,c]=kmeans(tempt,class);
                [~,I] = sort(sum(c,2),1);
                idxt=idx;

                ct=c;
                    for cng=1:size(I,1)
                        idx(idxt==I(cng))=cng;
                        c(cng,:)=ct(I(cng),:);
                    end

                     edgeflag=reshape(idx,[size(Img,1) size(Img,2)]); 
                 edgeflag2=double((edgeflag-1)/Norm); 
                  edgeflag3k=double((edgeflag-1)/2); 
%                   edgeflag3k(edgeflag3k==1/2)=0;
                % timk=Img;
                               [valk,idmax]=max(timk,[],3); 
                             k=size(Img,3);
%% Finding the Lambda(W1) parameter
                                 [ncf,hcf]=hist(valk(edgeflag2==1),linspace(min(valk(:)),max(valk(:)),100));
                                    ncf=ncf/sum(ncf);

                                [ncb,hcb]=hist(valk(edgeflag2==0.5),linspace(min(valk(:)),max(valk(:)),100));
                                    ncb=ncb/sum(ncb); 
                                    
nt= find(ncb>ncf,1,'last');
ht=hcb(nt); 
idmaxini=idmax;

overlap2=sum(valk(edgeflag2==1)<=ht)./sum(valk(edgeflag2==1)>ht);

                    edgeflag2B = padarray(edgeflag2',1,'symmetric');
                     edgeflag2IB = padarray(edgeflag2B',1,'symmetric');
                     
                     base1=find_base2(edgeflag2IB,3);  
                       class3=sum(base1==1,3);   

                      idmaxk=idmax;
                     
                         idmaxkB = padarray(idmaxk',1,'symmetric');
                     IB = padarray(idmaxkB',1,'symmetric');

                     base=find_base(IB,3);
                           Mold=mean(base,3); 
                         varold2=sum((base-repmat(Mold,[1 1 8])).^2,3);
                         
                         M10=idmaxk-Mold;
                          MD=Mold-Mold;

                           s01=sqrt((varold2+(M10).*(idmaxk-(Mold+(M10)./9)))./8);
                          
                           sD=sqrt((varold2+(MD).*(Mold-(Mold+(MD)./9)))./8);
                           
                           sgain=s01-sD;
                           dD=abs(idmax-Mold);
                           sg=sgain(class3>8 & edgeflag2==1);
                           dg=dD(class3>8 & edgeflag2==1);
                           
                          sgk=sg;
                           sg(sgk==0)=[];
                           dg(sgk==0)=[];
                           
                           if overlap2<0
                               overlap2=0;
                           elseif overlap2>1
                               overlap2=1;
                           end
%                           
                           
                           WA=dg./sg;
                           lambda1=abs(quantile(WA(:),overlap2));
                           
%                            if WW<9
%                                WW=9;
%                            end
                                  
%% Finding the Lambda(W1) parameter

meanfg=mean(valk(edgeflag2==1));
meansfg=mean(valk(edgeflag2==0.5));
meanbg=mean(valk(edgeflag2==0));


RT=(meansfg-meanbg)./(meanfg-meanbg);

CD=3/3;
C1=CD*1./lambda1;
C2=CD*RT./lambda1;
C3=CD*0./lambda1;

WW=1;

edgeflag3k(edgeflag2==1)=C1;  
edgeflag3k(edgeflag2==0.5)=C2; 
edgeflag3k(edgeflag2==0)=C3;  
                           
%                            if WW<9
%                                WW=9;
%                            end
                                  


%% Finding step size and stopping criteria(epsilon) relative to stack size
                KE= max(idmax(edgeflag2>0))- min(idmax(edgeflag2>0))+1;  
                step=KE/100;
%% Cost minimization           
        idmaxk=idmax;
       cost=[];         
             
              cost(2)=10;%2 fake values to enter while loop; to be ignored later
               cost(1)=100;
             iter=2;
             
                while abs((cost(iter)-cost(iter-1)))>((0.000001*KE))
                 
                    iter=iter+1;
                    idmax1=idmaxk+step;
                     idmax2=idmaxk-step;
                     
                      idmaxkB = padarray(idmaxk',1,'symmetric');
                     IB = padarray(idmaxkB',1,'symmetric');
                     
                     base=find_base(IB,3);
                           Mold=mean(base,3); 
                         varold2=sum((base-repmat(Mold,[1 1 8])).^2,3);
                     
                     d1=abs(idmax-idmax1).*edgeflag3k;
                       d2=abs(idmax-idmax2).*edgeflag3k;
                         d0=abs(idmax-idmaxk).*edgeflag3k;

                         M11=idmax1-Mold;
                         M12=idmax2-Mold;
                         M10=idmaxk-Mold;
                         
                             s1=WW*sqrt((varold2+(M11).*(idmax1-(Mold+(M11)./9)))./8);
                         s2=WW*sqrt((varold2+(M12).*(idmax2-(Mold+(M12)./9)))./8);
                          s0=WW*sqrt((varold2+(M10).*(idmaxk-(Mold+(M10)./9)))./8);
                                                 
                             c1=d1+s1;
                             c2=d2+s2;
                             c0=d0+s0;  
                          [minc,shiftc]=min(cat(3,c0,c1,c2),[],3);
      
                          shiftc=shiftc-1;
                          
                          shiftc(shiftc==1)=step;
                           shiftc(shiftc==2)=-step;
                          
                idmaxk=idmaxk+shiftc;
     cost(iter)=sum(abs(minc(:)))/(npxl);
     step=step*0.99;
                iter
                end

                cost(1:2)=[];
                               
qzr2=round(idmaxk);
A=Img;
imageSize=size(A);
%                          qzr2(qzr2>k)=k;
%                              qzr2(qzr2<1)=1;
% 
                           zprojf1=zeros(size(qzr2));
                           
                           for kin=min(qzr2(:)):max(qzr2(:))
                               temp=Img(:,:,kin);
                                zprojf1(qzr2==kin)=temp(qzr2==kin);                               
                           end
                                    
                       Manifold=idmaxk;%final Z map
                       OUTput_image=zprojf1;% composite image
                       Classmap=double(edgeflag);%kmeans class map
                       
%% PSI calculation
%      totalz=sum(timk(:));
%   sftz=[];
%   for k=1:size(A,3)
%       sft=k-1;      
%        qzr3=qzr2-sft;
%         qzr3(qzr3>imageSize(3))=imageSize(3);
%                              qzr3(qzr3<1)=1;
%                              
%                            zprojf1=zeros(size(qzr3));
%                            
%                            for kin=min(qzr3(:)):max(qzr3(:))
%                                temp=Img(:,:,kin);
%                                 zprojf1(qzr3==kin)=temp(qzr3==kin);                               
%                            end
%                                
%                       Gx = imfilter(zprojf1, M, 'replicate', 'conv');
%                       Gy = imfilter(zprojf1, M', 'replicate', 'conv');                                            
%                       zprojf1 = abs(Gx) + abs(Gy);                     
%                      zprojf2=zprojf1; 
% 
%                      qzr3=qzr2+sft;
%                    qzr3(qzr3>imageSize(3))=imageSize(3);
%                              qzr3(qzr3<1)=1;
%                            
%                            zprojf1=zeros(size(qzr3));
%                            
%                            for kin=min(qzr3(:)):max(qzr3(:))
%                                temp=Img(:,:,kin);
%                                 zprojf1(qzr3==kin)=temp(qzr3==kin);                               
%                            end
%                            
%                          Gx = imfilter(zprojf1, M, 'replicate', 'conv');
%                       Gy = imfilter(zprojf1, M', 'replicate', 'conv');                                            
%                       zprojf1 = abs(Gx) + abs(Gy);
% 
% sftz(k)=(sum(zprojf2(:))+sum(zprojf1(:)))/totalz;
%   end
% 
%   sftzm=sftz;
%   sftz(1:2)=[];
% sftz2=mat2gray(sftz);
% PSI=1-sum(sftz2)./length(sftz2);

