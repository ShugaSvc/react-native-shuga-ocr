
#import "RNShugaOcr.h"
//#import "FirebaseMLVision.h"
#import <FirebaseMLVision/FirebaseMLVision.h>

@implementation RNShugaOcr

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(scanTextInImage:(NSString *)data success:(RCTResponseSenderBlock)successCallback error:(RCTResponseSenderBlock)errorCallback) {
    NSData *bytes = [[NSData alloc]initWithBase64EncodedString:data options:NSDataBase64DecodingIgnoreUnknownCharacters];
    UIImage *image = [UIImage imageWithData:bytes];
    
    FIRVision *vision = [FIRVision vision];
    FIRVisionTextRecognizer *textRecognizer = [vision onDeviceTextRecognizer];
    FIRVisionImage *firimage = [[FIRVisionImage alloc] initWithImage:image];
    
//    FIRVisionImageMetadata *metadata = [[FIRVisionImageMetadata alloc] init];
//    metadata.orientation = FIRVisionDetectorImageOrientationRightTop;
//    firimage.metadata = metadata;
    
    [textRecognizer processImage:firimage completion:^(FIRVisionText *_Nullable result, NSError *_Nullable error) {
        if (error != nil) {
            errorCallback(@[error.debugDescription]);
            return;
        }
        
        if (result == nil) {
            errorCallback(@[[NSNull null]]);
            return;
        }
        dispatch_async (dispatch_get_main_queue(), ^{
            NSMutableArray *returnResult = [NSMutableArray new];
            for (FIRVisionTextBlock *block in result.blocks) {
                for (FIRVisionTextLine *line in block.lines) {
                    for (FIRVisionTextElement *element in line.elements) {
                        CGRect frame = element.frame;
                        NSArray *vertices = @[@{@"x": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.x)],
                                                @"y": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.y)]},
                                              @{@"x": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.x + frame.size.width)],
                                                @"y": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.y)]},
                                              @{@"x": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.x)],
                                                @"y": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.y + frame.size.height)]},
                                              @{@"x": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.x + frame.size.width)],
                                                @"y": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.y + frame.size.height)]},
                                            ];
                        NSDictionary *obj = @{
                                              @"description": element.text,
                                              @"boundingPoly": @{@"vertices": vertices}
                                              };
                        [returnResult addObject:obj];
                    }
                }
            }
            successCallback(@[returnResult]);
        });
    }];
}

@end
  
