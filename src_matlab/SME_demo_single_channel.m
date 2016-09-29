%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Makes a 2D reconstruction which is spatially continuous out of a 3D image volume
% Authors: ASM Shihavuddin(shihavud@biologie.ens.fr)
%          Sreetama Basu (sreetama.basu@ens.fr)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

clear all;close all;clc;
% Example 1:  % Runs experiment on single chanel image stack 
%
% Example 2:  % Runs experiment on multichannel chanel image stack; chooses
% one channel for construction of reference manifold, which is  then
% applied to data on all 3 channels to preserve spatial coherence
%% Read the TIF file

[FileName,PathName] = uigetfile({'*.tif';'*.tiff'},'Select the input tif file');
dname = PathName;

   ButtonName = questdlg('Imaging modality?', ...
                         'Imaging modality', ...
                         'Confocal', 'Widefield', 'Blue');
name{1} = FileName;
% name{2} = 'shRddendrites_cropped_1.tif'; %reference channel based on which the manifold is constructed and applied to all channels
%name{3} = 'shRddendrites_cropped_2.tif';

%%%%%%%%%%%%%%%%%%%example 1
kid=[1]; % set as 1 or 2
fname=name{kid};
mkdir([dname filesep strrep(fname,'.tif','') '_Results'])
nametex=[dname filesep strrep(fname,'.tif','') '_Results' filesep strrep(fname,'.tif','')];
nametex2=[dname filesep strrep(fname,'.tif','')];

info = imfinfo([PathName fname]);
num_images = numel(info);
Img1=[];
for k = 1:num_images
    I = imread([PathName fname], k);
    Img1(:,:,k)=I;   
end

% [Img11,zval11]=max(Img1,[],3);
% Img11=uint16(65535*mat2gray(Img11));
% imwrite(uint16(65535*mat2gray(Img11)),[nametex 'MIP_compI.png']);
% imwrite(uint16(65535*mat2gray(zval11)),[nametex 'MIP_zmap.png']);  
IM4=Img1; 
Img=Img1;
  fname1=strrep(fname,'.tif','');  
%  fname=strrep(fname,'_',' '); 

if strcmp(ButtonName,'Confocal')
[zprojf1,qzr2,classmap,idmaxini,cost,WW,C1,C2,C3]=Main_SME_method(Img1,nametex2); 
elseif strcmp(ButtonName,'Widefield')
[zprojf1,qzr2,classmap,idmaxini,cost,WW,C1,C2,C3]=Main_SME_method_SML(Img1,nametex2); 
end
   figure; 
            colormap(jet) 
            imagesc(qzr2);
%             axis tight
            caxis manual
            caxis([1 size(Img,3)]);
            
box off
axis off
set(gcf,'color','w');
            
            colorbar
            addpath([cd filesep 'export_fig-master']);
            export_fig([nametex 'SME_FinalZmap_with_colorbar.png'],'-a1', '-native','-p0','-png');
  
                C = colormap; 
                L = size(C,1);
                Gs = round(interp1(linspace(1,size(Img,3),L),1:L,double(qzr2)));
                Gs(isnan(Gs))=1;
       
                H = reshape(C(Gs,:),[size(Gs) 3]); 
                imwrite(uint16(65535*H),[nametex 'SME_FinalZmap.png']);
            close all  
            
             figure 
           colormap(jet)
            imagesc(idmaxini);
              
%             axis tight
box off
axis off
set(gcf,'color','w');
            caxis manual
            caxis([1 size(Img,3)]);
            colorbar   
            export_fig([nametex 'SME_InitialZmap_with_colorbar.png'],'-a1', '-native','-p0','-png');

  
                C = colormap; 
                L = size(C,1);
                Gs = round(interp1(linspace(1,size(Img,3),L),1:L,double(idmaxini)));
                Gs(isnan(Gs))=1;
       
                H = reshape(C(Gs,:),[size(Gs) 3]); 
                imwrite(uint16(65535*H),[nametex 'SME_InitialZmap.png']);
            close all  
 
              figure 
            colormap(cool) 
            imagesc(classmap);
            axis tight
            caxis manual
            caxis([1 3]);
  
                C = colormap; 
                L = size(C,1);
                Gs = round(interp1(linspace(1,3,L),1:L,double(classmap)));
                Gs(isnan(Gs))=1;
       
                H = reshape(C(Gs,:),[size(Gs) 3]); 
                imwrite(uint16(65535*H),[nametex 'Classmap.png']);
            close all  

                 zprojf1=uint16(65535*(mat2gray(zprojf1)));             
                composite_image=zprojf1;

                f=strcat(nametex,'SME_compositeImage','.png');
                imwrite(composite_image,f);
                
                f=strcat(nametex,'MIP_compositeImage','.png');
                imwrite(uint16(65535*(mat2gray(max(Img1,[],3)))),f);


% figure()
%     plot(1:length(sftz2),mat2gray(sftz2),'LineWidth',2.00,'Color',[0 0 0]);hold on;
%     xlim([1 length(sftz2)]);
%     xlabel('Distance from principal manifold','FontSize', 24,'FontName','Times');
%     ylabel('Information content', 'FontSize', 24,'FontName','Times') % y-axis label
% 
%     set(gca, 'Ticklength', [0 0])
%     set(gca, 'box', 'off')
%     ax = gca;
%                                                     
%     text(k*0.3, .85,['Projection suitability'],'FontSize', 22,'HorizontalAlignment','left','VerticalAlignment', 'top','FontName','Times');
%     text(k*0.3, .7,['index (PSI) = ' num2str(decay_ratio,'%.2f')],'FontSize', 22,'HorizontalAlignment','left','VerticalAlignment', 'top','FontName','Times');
%                                                                                  
%     ha = axes('Position',[0 0 1 1],'Xlim',[0 1],'Ylim',[0 1],'Box','off','Visible','off','Units','normalized', 'clipping' , 'off');
%                                                 %                                             text(0.5,1,['Comparison of nMI' ],'HorizontalAlignment','center','VerticalAlignment', 'top');
%     set(gcf,'PaperPositionMode','auto')
% 
%     print([nametex 'PSI.png'], '-dpng', '-r300');                                                            
%     set(gcf,'Units','inches'); 
                                             
figure()
iter=length(cost);
plot(1:iter, cost(1:iter),'LineWidth',2.00,'Color',[0 0 0]);
xlim([1 iter])
ylim([cost(iter) cost(1)])
hold on;                             
xlabel('Iteration','FontSize', 24,'FontName','Times');
 ylabel('Cost', 'FontSize', 24,'FontName','Times') % y-axis label

set(gca, 'Ticklength', [0 0])
set(gca, 'box', 'off')
ax = gca;
ha = axes('Position',[0 0 1 1],'Xlim',[0 1],'Ylim',[0 1],'Box','off','Visible','off','Units','normalized', 'clipping' , 'off');
                                                %                                             text(0.5,1,['Comparison of nMI' ],'HorizontalAlignment','center','VerticalAlignment', 'top');
text(0.5,1,['C1 = ' num2str(C1,'%.4f') ', C2 = ' num2str(C2,'%.4f')],'HorizontalAlignment','center','VerticalAlignment', 'top','FontName','Times','FontSize', 20);                                          
set(gcf,'PaperPositionMode','auto')

print([nametex 'Cost.png'], '-dpng', '-r150');                                                            
set(gcf,'Units','inches');  
 

 %%%%%%%%%%%%%%%%%%%%%%%%%%for multi-channel image%%%%%%%%%%%%   
 if(kid==2)
  fname=strrep(fname,'1','2');  
 Img2=[];
for k = 1:num_images
    I = imread([cd filesep 'Dataset' filesep fname], k);
      Img2(:,:,k)=I;   
end
                    zmap=round(qzr2);
                    zmap(zmap>k)=k;
                    zmap(zmap<1)=1;
                    zprojf2=FV1_make_projection_from_layer(Img2,zmap,0,0);
                    imwrite(uint16(65535*mat2gray(zprojf2)),[nametex 'SME_channel2.png']);
 end
close all

