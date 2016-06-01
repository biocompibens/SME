clear all
close all

%% Read the TIF file

name{1} = 'CellBorder1.tif';
name{2} = 'Centriole1.tif';
name{3} = 'Neuron1.tif';
name{4} = 'Neuron2.tif';
name{5} = 'shRd dendrites.lif - Series038 - C=2.tif';
name{6} = 'shRd dendrites.lif - Series038 - C=1.tif';
name{7} = 'Series22_green.tif';
name{8} = 'Series22_blue.tif';
name{9} = 'Series22_red.tif';
name{10} = 'Syn3_SNR2.tif';
name{11} = 'Syn_Tissue_SNR2.tif';
name{12} = 'SynCB_SNR3.tif';
name{13}='golgi.tif';
name{14}='CellBorder2.tif';
name{15}='CELLP53YFP2.tif';
name{16}='CELLMITORED2.tif';
name{17}='MEMBRANE3.tif';
name{18} = 'Syn_Tissue_SNR1.tif';
name{19} = 'Syn_Tissue_SNR3.tif';

for kid=[8];
mkdir([cd filesep 'Results11' filesep strrep(name{kid},'.tif','')])
nametex=[cd filesep 'Results11' filesep strrep(name{kid},'.tif','') filesep strrep(name{kid},'.tif','_')];

    fname=name{kid};

info = imfinfo([cd filesep 'Dataset' filesep fname]);
num_images = numel(info);

Img1=[];
for k = 1:num_images
    I = imread([cd filesep 'Dataset' filesep fname], k);
      Img1(:,:,k)=I;   
end

[Img11,zval11]=max(Img1,[],3);
Img11=uint16(65535*mat2gray(Img11));
imwrite(uint16(65535*mat2gray(Img11)),[nametex 'MIP.png']);
  
IM4=Img1; 
Img=Img1;

 fname=strrep(fname,'.tif','')  
  fname=strrep(fname,'_',' '); 

[zprojf1,qzr2,classmap,idmaxini,cost,WW,sftz2,decay]=Main_SME_method(Img1); 

   figure 
                      colormap(jet) 

            imagesc(qzr2);
            axis tight
            caxis manual
                        caxis([1 size(Img,3)]);
  
                C = colormap; 
                L = size(C,1);
                Gs = round(interp1(linspace(1,size(Img,3),L),1:L,double(qzr2)));
                Gs(isnan(Gs))=1;
       
                H = reshape(C(Gs,:),[size(Gs) 3]); 
                imwrite(uint16(65535*H),[nametex 'Finalzmap.png']);
            close all  
            
             figure 
                      colormap(jet) 

            imagesc(idmaxini);
            axis tight
            caxis manual
                        caxis([1 size(Img,3)]);
  
                C = colormap; 
                L = size(C,1);
                Gs = round(interp1(linspace(1,size(Img,3),L),1:L,double(idmaxini)));
                Gs(isnan(Gs))=1;
       
                H = reshape(C(Gs,:),[size(Gs) 3]); 
                imwrite(uint16(65535*H),[nametex 'Initialzmap.png']);
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

                f=strcat(nametex,'SME','.png');
                imwrite(composite_image,f);
                
zmap=round(qzr2);
                         zmap(zmap>k)=k;
                             zmap(zmap<1)=1;
zprojf2=FV1_make_projection_from_layer(Img1,zmap,0,0);
imwrite(uint16(65535*mat2gray(zprojf2)),[nametex 'SMEV.png']);


figure()
plot(1:length(sftz2),sftz2,'LineWidth',2.00,'Color',[0 0 0]);hold on;
    xlim([1 length(sftz2)])                                
                                            xlabel('Distance from principal manifold','FontSize', 24,'FontName','Times');
                                            ylabel('Information content', 'FontSize', 24,'FontName','Times') % y-axis label

                                                                                 set(gca, 'Ticklength', [0 0])
                                                                                 set(gca, 'box', 'off')
                                                ax = gca;
                                                    
                                                    text(k*0.3, .85,['Projection suitability'],'FontSize', 22,'HorizontalAlignment','left','VerticalAlignment', 'top','FontName','Times');
                                                      text(k*0.3, .7,['index (PSI) = ' num2str(dekay,'%.2f')],'FontSize', 22,'HorizontalAlignment','left','VerticalAlignment', 'top','FontName','Times');
                                     
%                                                
                                                ha = axes('Position',[0 0 1 1],'Xlim',[0 1],'Ylim',[0 1],'Box','off','Visible','off','Units','normalized', 'clipping' , 'off');
                                                %                                             text(0.5,1,['Comparison of nMI' ],'HorizontalAlignment','center','VerticalAlignment', 'top');
                                                                                            set(gcf,'PaperPositionMode','auto')

                                                        print([nametex 'Decay.png'], '-dpng', '-r300');                                                            
                                                         set(gcf,'Units','inches');  
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
                                                  text(0.5,1,['W = ' num2str(WW)],'HorizontalAlignment','center','VerticalAlignment', 'top','FontName','Times','FontSize', 20);                                          
                                                set(gcf,'PaperPositionMode','auto')

                                                        print([nametex 'Cost.png'], '-dpng', '-r300');                                                            
                                                         set(gcf,'Units','inches');  
 
close all
end

